package com.example.core.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.core.location.EmergencyLocationManager
import com.example.core.service.FusedLocationBackgroundService
import com.example.data.local.AppDatabase
import com.example.data.local.toDomain
import com.example.data.repository.AppSessionManager
import com.example.domain.model.EmergencyContact
import com.example.domain.model.GeoLocation
import com.example.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsDispatchReport(
    val totalAttempted: Int,
    val successCount: Int,
    val successfulContactNames: List<String>,
    val failedContactNames: List<String>,
    val messageSent: String,
    val isPermissionGranted: Boolean,
    val primaryContactPhone: String? = null,
    val locationUsed: GeoLocation? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object EmergencySmsDispatcher {

    private const val TAG = "EmergencySmsDispatcher"

    suspend fun dispatchEmergencySms(
        context: Context,
        customNote: String? = null,
        location: GeoLocation? = null
    ): SmsDispatchReport = withContext(Dispatchers.IO) {
        // Automatically ensure FusedLocation background service is active
        try {
            FusedLocationBackgroundService.startLocationService(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting FusedLocationBackgroundService: ${e.message}")
        }

        val db = AppDatabase.getDatabase(context)
        
        // 1. Fetch saved contacts from Room Database
        var contacts: List<EmergencyContact> = try {
            val enabled = db.emergencyContactDao().getSmsEnabledContactsSync()
            if (enabled.isNotEmpty()) enabled else db.emergencyContactDao().getAllContactsSync()
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts from DB: ${e.message}")
            emptyList()
        }



        val primaryPhone = contacts.firstOrNull()?.phone

        // 2. Fetch User Profile from Database or Session
        val userProfile: UserProfile? = try {
            db.userDao().getProfileSync()?.toDomain() ?: AppSessionManager.currentUser.value
        } catch (e: Exception) {
            AppSessionManager.currentUser.value
        }

        val userName = userProfile?.name ?: "RAMISA User"
        val bloodGroup = if (!userProfile?.bloodGroup.isNullOrBlank() && userProfile?.bloodGroup != "Unknown") {
            " (Blood: ${userProfile.bloodGroup})"
        } else ""

        // 3. Obtain Accurate Location Coordinates using FusedLocationProviderClient
        val targetLocation: GeoLocation = if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
            location
        } else {
            EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
        }

        val lat = targetLocation.latitude
        val lng = targetLocation.longitude
        val mapLink = "https://maps.google.com/?q=$lat,$lng"
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val customNoteText = if (!customNote.isNullOrBlank()) " Note: $customNote." else ""

        // Fetch battery level
        val batteryLevel = try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            val pct = bm?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 80
            " [Battery: $pct%]"
        } catch (e: Exception) {
            ""
        }

        // 4. Construct SMS Message Body with Fused Location link
        val messageBody = "🚨 [RAMISA SOS ALERT] আমি বিপদে আছি! $userName$bloodGroup needs immediate help!$customNoteText$batteryLevel Live GPS: $mapLink (at $timeStr, ±${String.format("%.0f", targetLocation.accuracyMeters)}m). Please call 999 or rescue immediately!"

        // 5. Check SEND_SMS Permission
        val hasSmsPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasSmsPermission) {
            Log.w(TAG, "SEND_SMS permission not granted. Cannot send physical SIM SMS.")
            return@withContext SmsDispatchReport(
                totalAttempted = contacts.size,
                successCount = 0,
                successfulContactNames = emptyList(),
                failedContactNames = contacts.map { "${it.name} (SMS Permission Required)" },
                messageSent = messageBody,
                isPermissionGranted = false,
                primaryContactPhone = primaryPhone,
                locationUsed = targetLocation
            )
        }

        // 6. Send Multipart SMS via SIM Card to ALL contacts
        val successNames = mutableListOf<String>()
        val failedNames = mutableListOf<String>()

        try {
            val smsManager: SmsManager = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            for (contact in contacts) {
                val cleanPhone = contact.phone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.isNotBlank()) {
                    var sentSuccessfully = false
                    try {
                        val dividedParts = smsManager.divideMessage(messageBody)
                        if (dividedParts.size > 1) {
                            smsManager.sendMultipartTextMessage(
                                cleanPhone,
                                null,
                                dividedParts,
                                null,
                                null
                            )
                        } else {
                            smsManager.sendTextMessage(
                                cleanPhone,
                                null,
                                messageBody,
                                null,
                                null
                            )
                        }
                        sentSuccessfully = true
                        successNames.add(contact.name)
                        Log.d(TAG, "SMS successfully dispatched to ${contact.name} ($cleanPhone)")
                    } catch (e: Exception) {
                        Log.w(TAG, "Primary SMS sending failed for ${contact.name}, trying default SmsManager: ${e.message}")
                        try {
                            @Suppress("DEPRECATION")
                            val fallbackSms = SmsManager.getDefault()
                            fallbackSms.sendTextMessage(cleanPhone, null, messageBody.take(160), null, null)
                            sentSuccessfully = true
                            successNames.add(contact.name)
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed to send SMS to ${contact.name} ($cleanPhone): ${e2.message}")
                            failedNames.add("${contact.name}: ${e2.message ?: "Failed"}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SmsManager error: ${e.message}")
            failedNames.add("SmsManager Error: ${e.message}")
        }

        SmsDispatchReport(
            totalAttempted = contacts.size,
            successCount = successNames.size,
            successfulContactNames = successNames,
            failedContactNames = failedNames,
            messageSent = messageBody,
            isPermissionGranted = true,
            primaryContactPhone = primaryPhone,
            locationUsed = targetLocation
        )
    }

    /**
     * Initiates an immediate phone call to primary emergency contact or Bangladesh National 999.
     */
    fun initiateEmergencyPhoneCall(context: Context, phoneNumber: String? = null) {
        try {
            val targetNumber = phoneNumber?.takeIf { it.isNotBlank() } ?: "999"
            val hasCallPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$targetNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$targetNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating emergency phone call: ${e.message}")
        }
    }

    /**
     * Sends automatic "Reached Safely" (আলহামদুলিল্লাহ নিরাপদে পৌঁছেছি) notification SMS to primary guardians.
     */
    suspend fun dispatchSafeArrivalSms(
        context: Context,
        destination: String
    ): Boolean = withContext(Dispatchers.IO) {
        val hasSmsPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasSmsPermission) return@withContext false

        try {
            val db = AppDatabase.getDatabase(context)
            val contacts = db.emergencyContactDao().getSmsEnabledContactsSync()
                .ifEmpty { db.emergencyContactDao().getAllContactsSync() }
            
            if (contacts.isEmpty()) return@withContext false

            val userProfile = db.userDao().getProfileSync()?.toDomain() ?: AppSessionManager.currentUser.value
            val userName = userProfile?.name ?: "RAMISA User"
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val message = "🟢 [RAMISA Safe Arrival] আলহামদুলিল্লাহ, $userName নিরাপদে $destination -এ পৌঁছেছেন (at $timeStr)। ধন্যবাদ!"

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            for (c in contacts.take(2)) {
                val cleanPhone = c.phone.replace(Regex("[^0-9+]"), "")
                if (cleanPhone.isNotBlank()) {
                    smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send safe arrival SMS: ${e.message}")
            false
        }
    }
}
