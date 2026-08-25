package com.example.features.lockscreen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.core.sms.EmergencySmsDispatcher
import com.example.core.sms.SmsDispatchReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object LockScreenSosManager {

  const val CHANNEL_ID = "ramisa_lockscreen_guard"
  const val NOTIFICATION_ID = 9991
  const val ACTION_TRIGGER_LOCKSCREEN_SOS = "com.example.ACTION_TRIGGER_LOCKSCREEN_SOS"
  const val ACTION_CALL_999 = "com.example.ACTION_CALL_999"
  const val EXTRA_OPEN_EMERGENCY = "extra_open_emergency"

  private val _isLockScreenGuardEnabled = MutableStateFlow(true)
  val isLockScreenGuardEnabled: StateFlow<Boolean> = _isLockScreenGuardEnabled.asStateFlow()

  private val _lastOfflineDispatchStatus = MutableStateFlow("Direct SIM SMS & Offline Guard Ready")
  val lastOfflineDispatchStatus: StateFlow<String> = _lastOfflineDispatchStatus.asStateFlow()

  private val _offlineSmsSentCount = MutableStateFlow(0)
  val offlineSmsSentCount: StateFlow<Int> = _offlineSmsSentCount.asStateFlow()

  fun setLockScreenGuardEnabled(context: Context, enabled: Boolean) {
    _isLockScreenGuardEnabled.value = enabled
    if (enabled) {
      showLockScreenNotification(context)
    } else {
      cancelLockScreenNotification(context)
    }
  }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "RAMISA Lock Screen Safety Guard"
      val descriptionText = "Persistent Lock Screen emergency trigger and offline SIM SMS dispatch"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        enableVibration(true)
      }
      val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun showLockScreenNotification(context: Context) {
    createNotificationChannel(context)

    // Intent to open emergency screen directly over lock screen
    val openEmergencyIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra(EXTRA_OPEN_EMERGENCY, true)
    }
    val openPendingIntent = PendingIntent.getActivity(
      context,
      101,
      openEmergencyIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent for direct offline SOS broadcast trigger
    val sosBroadcastIntent = Intent(context, LockScreenSosReceiver::class.java).apply {
      action = ACTION_TRIGGER_LOCKSCREEN_SOS
    }
    val sosPendingIntent = PendingIntent.getBroadcast(
      context,
      102,
      sosBroadcastIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent to dial 999 immediately
    val dial999Intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:999")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val dialPendingIntent = PendingIntent.getActivity(
      context,
      103,
      dial999Intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("🚨 RAMISA Lock Screen Safety Guard")
      .setContentText("Direct SIM SMS Guard Active • Tap below for instant Offline SOS")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("Phone locked or no internet? Tap '🚨 TRIGGER OFFLINE SOS' to send automated SIM SMS with live GPS to guardians without unlocking.")
      )
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on locked lockscreen
      .setOngoing(true)
      .setContentIntent(openPendingIntent)
      .addAction(
        android.R.drawable.ic_dialog_alert,
        "🚨 TRIGGER OFFLINE SOS",
        sosPendingIntent
      )
      .addAction(
        android.R.drawable.ic_menu_call,
        "CALL 999",
        dialPendingIntent
      )
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(NOTIFICATION_ID, notification)
  }

  fun cancelLockScreenNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID)
  }

  fun triggerOfflineEmergency(context: Context, note: String = "Lock Screen Trigger") {
    CoroutineScope(Dispatchers.IO).launch {
      // 1. Dispatch SIM SMS to all database emergency contacts
      val report: SmsDispatchReport = EmergencySmsDispatcher.dispatchEmergencySms(
        context = context,
        customNote = "Lock Screen Direct Trigger"
      )

      _offlineSmsSentCount.value += report.successCount
      val statusText = if (report.successfulContactNames.isNotEmpty()) {
        "Dispatched SIM SMS to ${report.successfulContactNames.joinToString(", ")}"
      } else {
        "Dispatched SIM SMS to ${report.totalAttempted} contacts"
      }
      _lastOfflineDispatchStatus.value = statusText

      // 2. Launch Emergency UI directly over lockscreen
      val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        putExtra(EXTRA_OPEN_EMERGENCY, true)
      }
      context.startActivity(launchIntent)
    }
  }
}
