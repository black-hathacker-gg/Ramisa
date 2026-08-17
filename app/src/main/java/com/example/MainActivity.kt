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
import com.example.features.lockscreen.LockScreenSosManager
import com.example.ui.theme.RamisaTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    configureLockScreenDisplay()

    // Initialize Lock Screen SOS notification guard
    LockScreenSosManager.showLockScreenNotification(this)

    val openEmergency = intent?.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false) ?: false

    setContent {
      RamisaTheme {
        val navController = rememberNavController()

        LaunchedEffect(openEmergency) {
          if (openEmergency) {
            navController.navigate(Screen.Emergency.route)
          }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
          RamisaNavGraph(
            navController = navController,
            startDestination = if (openEmergency) Screen.Emergency.route else Screen.Welcome.route
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val openEmergency = intent.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false)
    if (openEmergency) {
      configureLockScreenDisplay()
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


