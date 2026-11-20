package com.example.core.sms

import android.content.Context
import com.example.domain.model.GeoLocation
import org.json.JSONArray
import org.json.JSONObject

/** Private, synchronously committed ledger. No retry may infer success from an absent receipt. */
internal class SmsLedgerStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("ramisa_sms_ledger", Context.MODE_PRIVATE)
    private val events = linkedMapOf<String, SmsLedgerEvent>()

    init {
        val saved = preferences.getString("events", null)
        if (saved != null) {
            val array = JSONArray(saved) // Corruption fails closed: never silently discard submission history.
            for (i in 0 until array.length()) {
                val item = decode(array.getJSONObject(i))
                events[item.id] = SmsReceiptReducer.recoverAfterRestart(item)
            }
            persist()
        }
    }

    @Synchronized fun get(id: String): SmsLedgerEvent? = events[id]
    @Synchronized fun latest(): SmsLedgerEvent? = events.values.maxByOrNull { it.createdAt }

    @Synchronized fun insert(event: SmsLedgerEvent): SmsLedgerEvent {
        events[event.id]?.let { return it }
        events[event.id] = event
        persist()
        return event
    }

    @Synchronized fun update(id: String, change: (SmsLedgerEvent) -> SmsLedgerEvent): SmsLedgerEvent? {
        val old = events[id] ?: return null
        val updated = change(old)
        events[id] = updated
        persist()
        return updated
    }

    private fun persist() {
        // Keep the most recent 100 events; PendingIntent callbacks for older IDs are ignored.
        while (events.size > 100) events.remove(events.keys.first())
        val json = JSONArray()
        events.values.forEach { json.put(encode(it)) }
        check(preferences.edit().putString("events", json.toString()).commit()) {
            "Could not persist SMS submission state; sending stopped"
        }
    }

    private fun encode(event: SmsLedgerEvent): JSONObject = JSONObject().apply {
        put("id", event.id); put("message", event.message); put("createdAt", event.createdAt)
        put("permissionGranted", event.permissionGranted); put("blockedReason", event.blockedReason ?: JSONObject.NULL)
        event.location?.takeIf { it.isAvailable }?.let { location ->
            put("location", JSONObject().apply {
                put("latitude", location.latitude); put("longitude", location.longitude)
                put("address", location.addressName)
                if (location.accuracyMeters.isFinite()) put("accuracy", location.accuracyMeters.toDouble())
                put("timestamp", location.timestamp)
                // Persist wall-clock age only: elapsed realtime cannot be compared across boots.
                put("approximate", location.isApproximate)
            })
        }
        val recipients = JSONArray()
        for (recipient in event.recipients) {
            val parts = JSONArray()
            for (part in recipient.parts) {
                parts.put(JSONObject().apply {
                    put("index", part.index)
                    put("send", part.sendState.name)
                    put("delivery", part.deliveryState.name)
                    put("error", part.error ?: JSONObject.NULL)
                })
            }
            recipients.put(JSONObject().apply {
                put("id", recipient.id)
                put("name", recipient.name)
                put("phone", recipient.phone)
                put("parts", parts)
            })
        }
        put("recipients", recipients)
    }

    private fun decode(json: JSONObject): SmsLedgerEvent {
        val recipients = json.getJSONArray("recipients")
        val location = json.optJSONObject("location")?.let {
            GeoLocation(it.getDouble("latitude"), it.getDouble("longitude"), it.optString("address", "Device location"),
                it.optDouble("accuracy", Double.NaN).toFloat(), it.optLong("timestamp"),
                isApproximate = it.optBoolean("approximate"))
        }
        return SmsLedgerEvent(
            id = json.getString("id"), message = json.getString("message"), createdAt = json.getLong("createdAt"),
            permissionGranted = json.optBoolean("permissionGranted", true),
            blockedReason = if (json.isNull("blockedReason")) null else json.optString("blockedReason"), location = location,
            recipients = (0 until recipients.length()).map { index ->
                val recipient = recipients.getJSONObject(index)
                val parts = recipient.getJSONArray("parts")
                SmsRecipientLedger(recipient.getString("id"), recipient.getString("name"), recipient.getString("phone"),
                    (0 until parts.length()).map { partIndex ->
                        val part = parts.getJSONObject(partIndex)
                        SmsPartReceipt(part.getInt("index"), SmsSendState.valueOf(part.getString("send")),
                            SmsDeliveryState.valueOf(part.getString("delivery")),
                            if (part.isNull("error")) null else part.optString("error"))
                    })
            }
        )
    }
}
