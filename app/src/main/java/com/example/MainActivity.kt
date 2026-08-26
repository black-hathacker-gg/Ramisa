package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.KeyEvent
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.RamisaNavGraph
import com.example.core.navigation.Screen
import com.example.core.service.FusedLocationBackgroundService
import com.example.core.service.RamisaSafetyService
import com.example.features.lockscreen.LockScreenSosManager
import com.example.ui.theme.RamisaTheme

class MainActivity : ComponentActivity() {

  private var volumeClickCount = 0
  private var lastVolumeClickTime = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    configureLockScreenDisplay()

    // Start RAMISA Safety and Fused Location Background Services
    try {
      RamisaSafetyService.startService(this)
      if (com.example.core.location.EmergencyLocationManager.hasLocationPermission(this)) {
        FusedLocationBackgroundService.startLocationService(this)
      }
    } catch (e: Exception) {
      // Background services start gracefully
    }

    // Initialize Lock Screen SOS notification guard
    LockScreenSosManager.showLockScreenNotification(this)

    val action = intent?.action
    val openEmergency = intent?.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false) == true ||
        action == "com.example.ACTION_TRIGGER_QUICK_SOS"
    val openFakeCall = intent?.getBooleanExtra("EXTRA_OPEN_FAKE_CALL", false) == true ||
        action == "com.example.ACTION_TRIGGER_QUICK_FAKECALL"
    val openSafeJourney = intent?.getBooleanExtra("EXTRA_OPEN_SAFE_JOURNEY", false) ?: false

    setContent {
      RamisaTheme {
        val navController = rememberNavController()

        LaunchedEffect(openEmergency, openFakeCall, openSafeJourney) {
          if (openEmergency) {
            navController.navigate(Screen.Emergency.route)
          } else if (openFakeCall) {
            navController.navigate(Screen.FakeCall.route)
          } else if (openSafeJourney) {
            navController.navigate(Screen.SafeJourney.route)
          }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
          RamisaNavGraph(
            navController = navController,
            startDestination = when {
              openEmergency -> Screen.Emergency.route
              openFakeCall -> Screen.FakeCall.route
              openSafeJourney -> Screen.SafeJourney.route
              else -> Screen.Welcome.route
            }
          )
        }
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      val now = System.currentTimeMillis()
      if (now - lastVolumeClickTime < 1800) {
        volumeClickCount++
        if (volumeClickCount >= 3) {
          volumeClickCount = 0
          triggerHardwareSos()
          return true
        }
      } else {
        volumeClickCount = 1
      }
      lastVolumeClickTime = now
    }
    return super.onKeyDown(keyCode, event)
  }

  private fun triggerHardwareSos() {
    try {
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 500), -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 300, 200, 300, 200, 500), -1)
      }
      Toast.makeText(this, "🚨 Volume Trigger SOS Activated!", Toast.LENGTH_SHORT).show()
      
      // Dispatch emergency SMS immediately
      com.example.core.service.FusedLocationBackgroundService.startLocationService(this)
      val intent = Intent(this, MainActivity::class.java).apply {
        action = "com.example.ACTION_TRIGGER_QUICK_SOS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      startActivity(intent)
    } catch (e: Exception) {
      // Graceful handling
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val action = intent.action
    val openEmergency = intent.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false) ||
        action == "com.example.ACTION_TRIGGER_QUICK_SOS"
    val openFakeCall = intent.getBooleanExtra("EXTRA_OPEN_FAKE_CALL", false) ||
        action == "com.example.ACTION_TRIGGER_QUICK_FAKECALL"
    val openSafeJourney = intent.getBooleanExtra("EXTRA_OPEN_SAFE_JOURNEY", false)
    
    if (openEmergency) {
      configureLockScreenDisplay()
    }
    
    if (openEmergency || openFakeCall || openSafeJourney) {
      recreate() // Force compose to re-initialize with destination
    }
  }

  private fun configureLockScreenDisplay() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
      keyguardManager?.requestDismissKeyguard(this, null)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      )
    }
  }
}
