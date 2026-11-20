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
import com.example.core.community.CommunityManager
import com.example.core.location.EmergencyLocationManager
import com.example.core.sms.EmergencySmsDispatcher
import com.example.core.sms.SmsDispatchReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import android.os.SystemClock

object LockScreenSosManager {

  const val CHANNEL_ID = "ramisa_lockscreen_guard"
  const val NOTIFICATION_ID = 9991
  const val ACTION_TRIGGER_LOCKSCREEN_SOS = "com.example.ACTION_TRIGGER_LOCKSCREEN_SOS"
  const val ACTION_CALL_999 = "com.example.ACTION_CALL_999"
  const val EXTRA_OPEN_EMERGENCY = "extra_open_emergency"
  private const val PREFS = "lockscreen_safety"
  private const val GUARD = "guard_enabled"
  private const val DIRECT_SMS = "direct_sms_enabled"
  private const val VOLUME = "volume_shortcut_enabled"

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var lastTriggerElapsed = -10_000L
  private var receiptObserver: kotlinx.coroutines.Job? = null

  private val _isLockScreenGuardEnabled = MutableStateFlow(true)
  val isLockScreenGuardEnabled: StateFlow<Boolean> = _isLockScreenGuardEnabled.asStateFlow()

  private val _lastOfflineDispatchStatus = MutableStateFlow("SOS shortcut ready; configure enabled contacts and SMS permission")
  val lastOfflineDispatchStatus: StateFlow<String> = _lastOfflineDispatchStatus.asStateFlow()

  private val _offlineSmsSentCount = MutableStateFlow(0)
  val offlineSmsSentCount: StateFlow<Int> = _offlineSmsSentCount.asStateFlow()

  fun setLockScreenGuardEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(GUARD, enabled).apply()
    _isLockScreenGuardEnabled.value = enabled
    if (enabled) {
      showLockScreenNotification(context)
    } else {
      cancelLockScreenNotification(context)
    }
  }

  fun isGuardEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(GUARD, false)

  fun isDirectSmsEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(DIRECT_SMS, false)

  fun setDirectSmsEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(DIRECT_SMS, enabled).apply()
    showLockScreenNotification(context)
  }

  fun isVolumeShortcutEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(VOLUME, false)

  fun setVolumeShortcutEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(VOLUME, enabled).apply()
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
    if (!isGuardEnabled(context)) return
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

    val directSmsEnabled = isDirectSmsEnabled(context)
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("RAMISA Lock Screen Safety Guard")
      .setContentText("SOS shortcut • SMS requires enabled contacts and cellular service")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(if (directSmsEnabled) "Tap Send SOS SMS to request SMS to enabled contacts. Check RAMISA for delivery status."
            else "Open RAMISA to review an emergency alert or open the dialer for 999.")
      )
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on locked lockscreen
      .setOngoing(true)
      .setContentIntent(openPendingIntent)
      .addAction(
        android.R.drawable.ic_dialog_alert,
        if (directSmsEnabled) "SEND SOS SMS" else "REVIEW SOS",
        if (directSmsEnabled) sosPendingIntent else openPendingIntent
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

  fun triggerOfflineEmergency(context: Context, note: String = "Lock Screen Trigger", onComplete: (() -> Unit)? = null) {
    if (!isGuardEnabled(context) || !isDirectSmsEnabled(context)) {
      onComplete?.invoke()
      return
    }
    val now = SystemClock.elapsedRealtime()
    synchronized(this) {
      if (now - lastTriggerElapsed < 10_000L) {
        onComplete?.invoke()
        return
      }
      lastTriggerElapsed = now
    }
    scope.launch {
      try {
        val report = EmergencySmsDispatcher.dispatchEmergencySms(context, note)
        _offlineSmsSentCount.value = report.successCount
        _lastOfflineDispatchStatus.value = report.statusSummary
        receiptObserver?.cancel()
        receiptObserver = scope.launch {
          EmergencySmsDispatcher.lastDispatchEvent.filterNotNull().first { updated ->
            if (updated.eventId == report.eventId) {
              _offlineSmsSentCount.value = updated.successCount
              _lastOfflineDispatchStatus.value = updated.statusSummary
            }
            updated.eventId == report.eventId && updated.isTerminal
          }
        }
        // The shortcut requests only the user's enabled contacts; it does not publish a community post.
        runCatching {
          context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_EMERGENCY, true)
          })
        }
      } catch (error: Exception) {
        _lastOfflineDispatchStatus.value = "SOS could not be submitted. Open RAMISA to review permissions and contacts."
      } finally {
        onComplete?.invoke()
      }
    }
  }
}
