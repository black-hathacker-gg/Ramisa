package com.example

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.RamisaNavGraph
import com.example.core.navigation.Screen
import com.example.core.service.FusedLocationBackgroundService
import com.example.core.service.RamisaSafetyService
import com.example.features.lockscreen.LockScreenSosManager
import com.example.ui.theme.RamisaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private var volumeClickCount = 0
  private var lastVolumeClickTime = 0L
  private val pendingRoute = MutableStateFlow<String?>(null)

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    if (fine || coarse) {
      com.example.core.location.EmergencyLocationManager.startBackgroundLocationUpdates(this)
      FusedLocationBackgroundService.startLocationService(this)
    }
  }

  override fun onResume() {
    super.onResume()
    if (com.example.core.location.EmergencyLocationManager.hasLocationPermission(this)) {
      com.example.core.location.EmergencyLocationManager.startBackgroundLocationUpdates(this)
      FusedLocationBackgroundService.startLocationService(this)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Ask for SMS, location and microphone access only from the feature the user starts.
    com.example.core.sms.EmergencySmsDispatcher.initialize(applicationContext)

    val action = intent?.action
    val isMainLauncher = intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true || action == Intent.ACTION_MAIN
    val openEmergency = (!isMainLauncher) && (intent?.getBooleanExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY, false) == true ||
        action == "com.example.ACTION_TRIGGER_QUICK_SOS")
    val openFakeCall = (!isMainLauncher) && (intent?.getBooleanExtra("EXTRA_OPEN_FAKE_CALL", false) == true ||
        action == "com.example.ACTION_TRIGGER_QUICK_FAKECALL")
    val openSafeJourney = (!isMainLauncher) && (intent?.getBooleanExtra("EXTRA_OPEN_SAFE_JOURNEY", false) ?: false)

    // Clear intent extras so future app opens don't re-trigger
    intent?.removeExtra(LockScreenSosManager.EXTRA_OPEN_EMERGENCY)
    intent?.removeExtra("EXTRA_OPEN_FAKE_CALL")
    intent?.removeExtra("EXTRA_OPEN_SAFE_JOURNEY")

    // ONLY configure lock screen flags if specifically invoked for an emergency on lockscreen
    if (openEmergency) {
      configureLockScreenDisplay()
    }

    // Start background services asynchronously so UI thread renders immediately without black screen
    CoroutineScope(Dispatchers.Default).launch {
      try {
        RamisaSafetyService.startService(applicationContext)
        if (com.example.core.location.EmergencyLocationManager.hasLocationPermission(applicationContext)) {
          FusedLocationBackgroundService.startLocationService(applicationContext)
        }
        LockScreenSosManager.showLockScreenNotification(applicationContext)
      } catch (e: Exception) {
        // Safe fallback
      }
    }

    val showDemoClassifier = BuildConfig.DEBUG && intent?.getBooleanExtra("preview_acoustic_classifier", false) == true
    val initialStartDestination = when {
      showDemoClassifier -> Screen.ThreatGuard.route
      openEmergency -> Screen.Emergency.route
      openFakeCall -> Screen.FakeCall.route
      openSafeJourney -> Screen.SafeJourney.route
      else -> {
        if (com.example.core.utils.GhostModeManager.isGhostModeEnabled(this)) {
          Screen.StealthMode.route
        } else {
          Screen.Welcome.route
        }
      }
    }

    setContent {
      RamisaTheme {
        val navController = rememberNavController()
        val routeToNavigate by pendingRoute.collectAsState()

        LaunchedEffect(routeToNavigate) {
          routeToNavigate?.let { target ->
            navController.navigate(target) {
              launchSingleTop = true
            }
            pendingRoute.value = null
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          RamisaNavGraph(
            navController = navController,
            startDestination = initialStartDestination
          )
        }
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (event?.repeatCount == 0 && LockScreenSosManager.isVolumeShortcutEnabled(this) &&
        (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
      val now = SystemClock.elapsedRealtime()
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
      Toast.makeText(this, "Review your emergency alert", Toast.LENGTH_SHORT).show()
      
      FusedLocationBackgroundService.startLocationService(this)
      pendingRoute.value = Screen.Emergency.route
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
      pendingRoute.value = Screen.Emergency.route
    } else if (openFakeCall) {
      pendingRoute.value = Screen.FakeCall.route
    } else if (openSafeJourney) {
      pendingRoute.value = Screen.SafeJourney.route
    }
  }

  private fun configureLockScreenDisplay() {
    try {
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
    } catch (e: Exception) {
      // Safe fallback
    }
  }
}
