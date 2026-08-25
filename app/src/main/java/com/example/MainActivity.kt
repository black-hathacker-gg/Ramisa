package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    configureLockScreenDisplay()

    // Start RAMISA Safety and Fused Location Background Services
    try {
      RamisaSafetyService.startService(this)
      FusedLocationBackgroundService.startLocationService(this)
    } catch (e: Exception) {
      // Background services start gracefully
    }

    // Initialize Lock Screen SOS notification guard
    LockScreenSosManager.showLockScreenNotification(this)

    val openEmergency = intent?.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false) ?: false
    val openFakeCall = intent?.getBooleanExtra("EXTRA_OPEN_FAKE_CALL", false) ?: false
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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val openEmergency = intent.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false)
    val openFakeCall = intent.getBooleanExtra("EXTRA_OPEN_FAKE_CALL", false)
    val openSafeJourney = intent.getBooleanExtra("EXTRA_OPEN_SAFE_JOURNEY", false)
    
    if (openEmergency) {
      configureLockScreenDisplay()
    }
    
    if (openEmergency || openFakeCall || openSafeJourney) {
      recreate() // Simple way to force compose to pick up the new intent
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
