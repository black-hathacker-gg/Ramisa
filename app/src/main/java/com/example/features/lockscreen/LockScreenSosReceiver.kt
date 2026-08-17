package com.example.features.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LockScreenSosReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Log.d("LockScreenSosReceiver", "Received intent action: ${intent.action}")
    when (intent.action) {
      LockScreenSosManager.ACTION_TRIGGER_LOCKSCREEN_SOS -> {
        LockScreenSosManager.triggerOfflineEmergency(context, "Lock Screen Notification Action Trigger")
      }
      Intent.ACTION_BOOT_COMPLETED,
      Intent.ACTION_MY_PACKAGE_REPLACED,
      Intent.ACTION_SCREEN_OFF,
      Intent.ACTION_USER_PRESENT -> {
        if (LockScreenSosManager.isLockScreenGuardEnabled.value) {
          LockScreenSosManager.showLockScreenNotification(context)
        }
      }
    }
  }
}
