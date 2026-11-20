package com.example.core.sms

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.core.location.EmergencyLocationManager
import com.example.data.local.AppDatabase
import com.example.data.local.toDomain
import com.example.data.repository.AppSessionManager
import com.example.domain.model.GeoLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

data class SmsRecipientReport(
    val contactId: String, val contactName: String, val totalParts: Int,
    val sentParts: Int, val deliveredParts: Int, val failedParts: Int,
    val outcomeUnknown: Boolean, val errors: List<String>
)

data class SmsDispatchReport(
    val totalAttempted: Int,
    /** Contacts for which ALL parts have successful network sent callbacks. */
    val successCount: Int,
    val successfulContactNames: List<String>, val failedContactNames: List<String>,
    val messageSent: String, val isPermissionGranted: Boolean,
    val primaryContactPhone: String? = null, val locationUsed: GeoLocation? = null,
    val timestamp: Long = System.currentTimeMillis(), val eventId: String = "",
    val pendingCount: Int = 0, val deliveredCount: Int = 0, val unknownCount: Int = 0,
    val statusSummary: String = "No SMS send confirmation", val isTerminal: Boolean = false,
    val recipientReports: List<SmsRecipientReport> = emptyList()
)

object EmergencySmsDispatcher {
    internal const val ACTION_SENT = "com.example.core.sms.SENT"
    internal const val ACTION_DELIVERED = "com.example.core.sms.DELIVERED"
    private val dispatchMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var ledger: SmsLedgerStore? = null
    private val _lastDispatchEvent = MutableStateFlow<SmsDispatchReport?>(null)
    val lastDispatchEvent: StateFlow<SmsDispatchReport?> = _lastDispatchEvent.asStateFlow()
    private val _reports = MutableStateFlow<Map<String, SmsDispatchReport>>(emptyMap())
    val reports: StateFlow<Map<String, SmsDispatchReport>> = _reports.asStateFlow()

    fun initialize(context: Context) {
        scope.launch {
            runCatching {
                store(context).latest()?.let { last ->
                    publish(last, showBanner = last.recipients.any { it.pending })
                }
            }
        }
    }
    internal fun restoreReport(context: Context, eventId: String): SmsDispatchReport? =
        store(context).get(eventId)?.let { publish(it, showBanner = false) }
    private fun store(context: Context): SmsLedgerStore = ledger ?: synchronized(this) {
        ledger ?: SmsLedgerStore(context.applicationContext).also { ledger = it }
    }
    fun clearLastDispatchEvent() { _lastDispatchEvent.value = null }

    /** Submits at most once per stable eventId. Return is not a send or delivery confirmation. */
    suspend fun dispatchEmergencySms(
        context: Context, customNote: String? = null, location: GeoLocation? = null,
        eventId: String = UUID.randomUUID().toString()
    ): SmsDispatchReport = dispatch(context, customNote, location, eventId, null)

    private suspend fun dispatch(
        context: Context, customNote: String?, location: GeoLocation?, eventId: String,
        safeArrivalDestination: String?
    ): SmsDispatchReport = withContext(Dispatchers.IO) {
        dispatchMutex.withLock {
            val appContext = context.applicationContext
            val journal = try { store(appContext) } catch (_: Exception) {
                return@withLock failureReport(eventId, "Saved SMS state is unavailable; sending stopped")
            }
            journal.get(eventId)?.let { return@withLock publish(it) }
            val contacts = try {
                // Never fall back to contacts who disabled SMS.
                AppDatabase.getDatabase(appContext).emergencyContactDao().getSmsEnabledContactsSync()
                    .distinctBy { normalizePhone(it.phone) }
            } catch (_: Exception) {
                return@withLock failureReport(eventId, "Cannot read saved emergency contacts; no SMS submitted")
            }
            val hasPermission = ContextCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
            val smsFeature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                PackageManager.FEATURE_TELEPHONY_MESSAGING else PackageManager.FEATURE_TELEPHONY
            var blocked = SmsPreflight.blockedReason(contacts.size, hasPermission,
                appContext.packageManager.hasSystemFeature(smsFeature))
            val manager = if (blocked == null) try { smsManager(appContext) } catch (_: Exception) { null } else null
            if (blocked == null && manager == null) blocked = "No default SMS SIM is available; select an active SIM before sending"
            val targetLocation = if (safeArrivalDestination != null || blocked != null ||
                !EmergencyLocationManager.hasLocationPermission(appContext)) null else
                location?.takeIf { it.isAvailable } ?: withTimeoutOrNull(7_000L) {
                    EmergencyLocationManager.getAccurateEmergencyCoordinates(appContext)
                }?.takeIf { it.isAvailable }
            val profile = try {
                AppDatabase.getDatabase(appContext).userDao().getProfileSync()?.toDomain() ?: AppSessionManager.currentUser.value
            } catch (_: Exception) { AppSessionManager.currentUser.value }
            val userName = profile?.name?.takeIf { it.isNotBlank() } ?: "RAMISA user"
            val message = if (safeArrivalDestination != null) {
                "[RAMISA] $userName reports arriving safely at $safeArrivalDestination."
            } else buildString {
                append("SOS! $userName needs help.")
                customNote?.takeIf { it.isNotBlank() }?.let { append(" Note: ${it.take(300)}.") }
                val battery = (appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager)
                    ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (battery != null && battery in 0..100) append(" Battery: $battery%.")
                if (targetLocation != null) {
                    append(" Location: https://maps.google.com/?q=${targetLocation.latitude},${targetLocation.longitude}.")
                    append(" ${targetLocation.description()}.")
                } else append(" Location unavailable; contact me or my trusted contacts.")
            }
            val divided = try { manager?.divideMessage(message)?.takeIf { it.isNotEmpty() } ?: arrayListOf(message) }
                catch (_: Exception) { return@withLock failureReport(eventId, "Cannot prepare SMS for the selected SIM; no SMS submitted") }
            val event = SmsLedgerEvent(
                id = eventId, message = message, permissionGranted = hasPermission, blockedReason = blocked, location = targetLocation,
                recipients = contacts.map { contact ->
                    val phone = normalizePhone(contact.phone)
                    val error = blocked ?: if (!isUsablePhone(phone)) "Invalid contact phone number" else null
                    SmsRecipientLedger(contact.id, contact.name, phone, divided.indices.map { part ->
                        SmsPartReceipt(part, if (error == null) SmsSendState.PREPARED else SmsSendState.FAILED, error = error)
                    })
                }
            )
            try { journal.insert(event) } catch (_: Exception) {
                return@withLock failureReport(eventId, "Could not save SMS submission state; no SMS submitted")
            }
            publish(event)
            if (blocked != null || manager == null) return@withLock publish(event)
            for (recipient in event.recipients) {
                if (recipient.failed) continue
                // Persist before the modem boundary. Crash/timeout is UNKNOWN, never an automatic retry.
                journal.update(eventId) { SmsReceiptReducer.submitted(it, recipient.id) }?.let { publish(it) }
                try {
                    val sent = ArrayList(divided.indices.map { receiptIntent(appContext, eventId, recipient.id, it, ACTION_SENT) })
                    val delivered = ArrayList(divided.indices.map { receiptIntent(appContext, eventId, recipient.id, it, ACTION_DELIVERED) })
                    manager.sendMultipartTextMessage(recipient.phone, null, ArrayList(divided), sent, delivered)
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) {
                    val definite = error is SecurityException || error is IllegalArgumentException || error is UnsupportedOperationException
                    journal.update(eventId) {
                        SmsReceiptReducer.submissionFailed(it, recipient.id,
                            if (definite) "SMS submission rejected (${error.javaClass.simpleName})" else "SMS submission outcome unknown; not automatically retried",
                            uncertain = !definite)
                    }?.let { publish(it) }
                }
            }
            scope.launch {
                delay(60_000L)
                journal.update(eventId) { current -> current.copy(recipients = current.recipients.map { recipient ->
                    recipient.copy(parts = recipient.parts.map { part ->
                        if (part.sendState == SmsSendState.SUBMITTED || part.sendState == SmsSendState.PREPARED)
                            part.copy(sendState = SmsSendState.UNKNOWN, error = "Send confirmation not received; not automatically retried")
                        else part
                    })
                }) }?.let { publish(it) }
            }
            publish(journal.get(eventId) ?: event)
        }
    }

    @Suppress("DEPRECATION")
    private fun smsManager(context: Context): SmsManager? {
        val subscription = SmsManager.getDefaultSmsSubscriptionId()
        if (subscription < 0 || subscription == Int.MAX_VALUE) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(SmsManager::class.java)?.createForSubscriptionId(subscription)
        else SmsManager.getSmsManagerForSubscriptionId(subscription)
    }

    private fun receiptIntent(context: Context, event: String, recipient: String, part: Int, action: String): PendingIntent {
        val intent = Intent(context, SmsStatusReceiver::class.java).apply {
            this.action = action
            data = Uri.Builder().scheme("ramisa-sms").authority("receipt").appendPath(event)
                .appendPath(recipient).appendPath(part.toString()).appendPath(if (action == ACTION_SENT) "sent" else "delivery").build()
        }
        // Mutable only for system-added PDU/error extras; the receiver destination is always explicit.
        val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or mutable)
    }

    internal fun recordReceipt(context: Context, intent: Intent, resultCode: Int) {
        val uri = intent.data ?: return
        val path = uri.pathSegments
        if (uri.scheme != "ramisa-sms" || uri.authority != "receipt" || path.size != 4) return
        val part = path[2].toIntOrNull() ?: return
        val journal = store(context)
        val updated = when (intent.action) {
            ACTION_SENT -> if (path[3] != "sent") null else journal.update(path[0]) {
                SmsReceiptReducer.sent(it, path[1], part, resultCode == Activity.RESULT_OK,
                    if (resultCode == Activity.RESULT_OK) null else "Network rejected SMS (code $resultCode)")
            }
            ACTION_DELIVERED -> {
                if (path[3] != "delivery") return
                val pdu = intent.getByteArrayExtra("pdu")
                val format = intent.getStringExtra("format") ?: "3gpp"
                val status = pdu?.let { android.telephony.SmsMessage.createFromPdu(it, format)?.status }
                val confirmed = resultCode == Activity.RESULT_OK &&
                    ((format == "3gpp" && status == 0) || (format == "3gpp2" && status == (2 shl 16)))
                val failed = resultCode != Activity.RESULT_OK ||
                    (format == "3gpp" && status != null && status in 0x40..0x7f) ||
                    (format == "3gpp2" && status != null && ((status ushr 24) and 3) == 3)
                journal.update(path[0]) {
                    SmsReceiptReducer.delivery(it, path[1], part, confirmed, failed,
                        if (confirmed) null else "Delivery not confirmed${status?.let { code -> " (report $code)" } ?: "; report unavailable"}")
                }
            }
            else -> null
        }
        updated?.let { publish(it) }
    }

    @Synchronized
    private fun publish(snapshot: SmsLedgerEvent, showBanner: Boolean = true): SmsDispatchReport {
        // A late publisher must not overwrite a newer receipt processed by another thread.
        val event = ledger?.get(snapshot.id) ?: snapshot
        val success = event.recipients.filter { it.sentConfirmed }
        val failures = event.recipients.filter { it.failed }
        val pending = event.recipients.count { it.pending }
        val unknown = event.recipients.count { it.unknown }
        val delivered = event.recipients.count { it.deliveredConfirmed }
        val summary = event.blockedReason ?: buildString {
            append("Send confirmed: ${success.size}/${event.recipients.size} contacts. Delivery confirmed: $delivered.")
            if (pending > 0) append(" Awaiting network send confirmation: $pending.")
            if (failures.isNotEmpty()) append(" Failed or partially failed: ${failures.size}.")
            if (unknown > 0) append(" Outcome unknown: $unknown; not automatically retried.")
        }
        val report = SmsDispatchReport(event.recipients.size, success.size, success.map { it.name },
            failures.map { it.name }, event.message, event.permissionGranted, event.recipients.firstOrNull()?.phone,
            event.location, event.createdAt, event.id, pending, delivered, unknown, summary, pending == 0,
            event.recipients.map { recipient ->
                SmsRecipientReport(recipient.id, recipient.name, recipient.parts.size,
                    recipient.parts.count { it.sendState == SmsSendState.SENT || it.deliveryState == SmsDeliveryState.DELIVERED },
                    recipient.parts.count { it.deliveryState == SmsDeliveryState.DELIVERED },
                    recipient.parts.count { it.sendState == SmsSendState.FAILED || it.deliveryState == SmsDeliveryState.FAILED },
                    recipient.unknown, recipient.parts.mapNotNull { it.error }.distinct())
            })
        _reports.update { it + (report.eventId to report) }
        if (showBanner && (_lastDispatchEvent.value?.timestamp ?: Long.MIN_VALUE) <= report.timestamp) {
            _lastDispatchEvent.value = report
        }
        return report
    }

    private fun failureReport(eventId: String, reason: String): SmsDispatchReport =
        SmsDispatchReport(0, 0, emptyList(), emptyList(), "", false, eventId = eventId,
            statusSummary = reason, isTerminal = true).also { report ->
            _reports.update { it + (eventId to report) }; _lastDispatchEvent.value = report
        }
    internal fun normalizePhone(value: String): String = value.filter { it.isDigit() || it == '+' }
    internal fun isUsablePhone(value: String): Boolean = Regex("\\+?[0-9]{7,15}").matches(value)

    /** Explicit user taps only: opens the dialer and NEVER places a call or chooses a fallback number. */
    fun initiateEmergencyPhoneCall(context: Context, phoneNumber: String? = null) {
        val number = phoneNumber?.let(::normalizePhone)?.takeIf { it.isNotBlank() } ?: return
        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
    suspend fun dispatchSafeArrivalSms(context: Context, destination: String): Boolean {
        val id = "arrival-${UUID.randomUUID()}"
        val initial = dispatch(context, null, null, id, destination)
        val completed = if (initial.isTerminal) initial else withTimeoutOrNull(60_000L) {
            reports.map { it[id] }.first { it?.isTerminal == true }
        }
        return completed != null && completed.totalAttempted > 0 && completed.successCount == completed.totalAttempted
    }
}
