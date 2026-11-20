package com.example.core.sms

/** State changes in this file never perform telephony operations. */
enum class SmsSendState { PREPARED, SUBMITTED, SENT, FAILED, UNKNOWN }
enum class SmsDeliveryState { UNCONFIRMED, DELIVERED, FAILED }

data class SmsPartReceipt(
    val index: Int,
    val sendState: SmsSendState = SmsSendState.PREPARED,
    val deliveryState: SmsDeliveryState = SmsDeliveryState.UNCONFIRMED,
    val error: String? = null
)

data class SmsRecipientLedger(
    val id: String,
    val name: String,
    val phone: String,
    val parts: List<SmsPartReceipt>
) {
    val sentConfirmed: Boolean get() = parts.isNotEmpty() && parts.all {
        it.sendState == SmsSendState.SENT || it.deliveryState == SmsDeliveryState.DELIVERED
    }
    val deliveredConfirmed: Boolean get() = parts.isNotEmpty() && parts.all {
        it.deliveryState == SmsDeliveryState.DELIVERED
    }
    val pending: Boolean get() = parts.any {
        it.sendState == SmsSendState.PREPARED || it.sendState == SmsSendState.SUBMITTED
    }
    val unknown: Boolean get() = parts.any { it.sendState == SmsSendState.UNKNOWN }
    val failed: Boolean get() = parts.any {
        it.sendState == SmsSendState.FAILED || it.deliveryState == SmsDeliveryState.FAILED
    }
}

data class SmsLedgerEvent(
    val id: String,
    val message: String,
    val recipients: List<SmsRecipientLedger>,
    val createdAt: Long = System.currentTimeMillis(),
    val permissionGranted: Boolean = true,
    val blockedReason: String? = null,
    val location: com.example.domain.model.GeoLocation? = null
)

object SmsReceiptReducer {
    fun submitted(event: SmsLedgerEvent, recipientId: String): SmsLedgerEvent =
        updateRecipient(event, recipientId) { part ->
            if (part.sendState == SmsSendState.PREPARED) part.copy(sendState = SmsSendState.SUBMITTED) else part
        }

    fun submissionFailed(event: SmsLedgerEvent, recipientId: String, reason: String, uncertain: Boolean): SmsLedgerEvent =
        updateRecipient(event, recipientId) { part ->
            if (part.sendState == SmsSendState.SENT || part.deliveryState == SmsDeliveryState.DELIVERED) part
            else part.copy(sendState = if (uncertain) SmsSendState.UNKNOWN else SmsSendState.FAILED, error = reason)
        }

    fun sent(event: SmsLedgerEvent, recipientId: String, partIndex: Int, successful: Boolean, error: String?): SmsLedgerEvent =
        updatePart(event, recipientId, partIndex) { part ->
            // A confirmed receipt cannot be undone by a duplicate/stale failure callback.
            if (part.sendState == SmsSendState.SENT || part.deliveryState == SmsDeliveryState.DELIVERED) part
            else part.copy(sendState = if (successful) SmsSendState.SENT else SmsSendState.FAILED,
                error = if (successful) null else error ?: "SMS sending failed")
        }

    fun delivery(event: SmsLedgerEvent, recipientId: String, partIndex: Int, confirmed: Boolean, failed: Boolean, error: String?): SmsLedgerEvent =
        updatePart(event, recipientId, partIndex) { part ->
            when {
                part.deliveryState == SmsDeliveryState.DELIVERED -> part
                confirmed -> part.copy(sendState = SmsSendState.SENT, deliveryState = SmsDeliveryState.DELIVERED, error = null)
                failed -> part.copy(deliveryState = SmsDeliveryState.FAILED, error = error ?: "Delivery failed")
                else -> part.copy(error = error ?: part.error)
            }
        }

    fun recoverAfterRestart(event: SmsLedgerEvent): SmsLedgerEvent = event.copy(
        recipients = event.recipients.map { recipient -> recipient.copy(parts = recipient.parts.map { part ->
            if (part.sendState == SmsSendState.PREPARED || part.sendState == SmsSendState.SUBMITTED)
                part.copy(sendState = SmsSendState.UNKNOWN, error = "No send confirmation before app restart; not automatically retried")
            else part
        }) }
    )

    private fun updateRecipient(event: SmsLedgerEvent, id: String, change: (SmsPartReceipt) -> SmsPartReceipt) =
        event.copy(recipients = event.recipients.map { if (it.id == id) it.copy(parts = it.parts.map(change)) else it })

    private fun updatePart(event: SmsLedgerEvent, id: String, index: Int, change: (SmsPartReceipt) -> SmsPartReceipt) =
        event.copy(recipients = event.recipients.map { recipient ->
            if (recipient.id == id) recipient.copy(parts = recipient.parts.map { if (it.index == index) change(it) else it }) else recipient
        })
}

object SmsPreflight {
    fun blockedReason(enabledContactCount: Int, permissionGranted: Boolean, smsHardwareAvailable: Boolean,
        defaultSimAvailable: Boolean = true): String? = when {
        enabledContactCount == 0 -> "No SMS-enabled emergency contacts are saved; no SMS submitted"
        !permissionGranted -> "SMS permission denied; no SMS submitted"
        !smsHardwareAvailable -> "This device has no SMS modem; no SMS submitted"
        !defaultSimAvailable -> "No default SMS SIM is available; no SMS submitted"
        else -> null
    }
}
