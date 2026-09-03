with open("app/src/main/java/com/example/core/sms/EmergencySmsDispatcher.kt", "r") as f:
    content = f.read()

# Add notification helper in EmergencySmsDispatcher
notif_import = """import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.widget.Toast
"""

if "import android.app.NotificationChannel" not in content:
    content = notif_import + content

# Add system notification logic when SMS dispatch completes
notif_logic = """        // Show Toast & System Notification on successful dispatch
        if (successNames.isNotEmpty()) {
            try {
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val channelId = "ramisa_sms_dispatch_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notifManager != null) {
                    val channel = NotificationChannel(
                        channelId,
                        "Emergency SMS Dispatch Confirmations",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Shows confirmation when Emergency SMS is dispatched via SIM"
                        enableVibration(true)
                    }
                    notifManager.createNotificationChannel(channel)
                }

                val notif = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("✓ RAMISA: Emergency SMS Dispatched")
                    .setContentText("Offline SMS successfully sent to ${successNames.size} guardians: ${successNames.joinToString(", ")}")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("🚨 Emergency SMS with live Fused GPS location has been dispatched via SIM card to:\\n${successNames.joinToString("\\n• ")}"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notifManager?.notify(1099, notif)
            } catch (e: Exception) {
                Log.e(TAG, "Error posting SMS confirmation notification: ${e.message}")
            }
        }

        SmsDispatchReport("""

content = content.replace("SmsDispatchReport(", notif_logic, 1)

with open("app/src/main/java/com/example/core/sms/EmergencySmsDispatcher.kt", "w") as f:
    f.write(content)

print("Updated EmergencySmsDispatcher with system notification")
