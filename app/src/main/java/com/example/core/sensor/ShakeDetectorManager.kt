package com.example.core.sensor

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.MainActivity
import com.example.core.community.CommunityManager
import com.example.core.location.EmergencyLocationManager
import com.example.core.sms.EmergencySmsDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Hardware Accelerometer Shake Detector that triggers emergency SOS automatically
 * when the device is vigorously shaken 3+ times in a crisis situation.
 */
object ShakeDetectorManager : SensorEventListener {

    private const val TAG = "ShakeDetectorManager"
    private const val SHAKE_THRESHOLD_GRAVITY = 2.7F
    private const val SHAKE_SLOP_TIME_MS = 500
    private const val SHAKE_COUNT_RESET_TIME_MS = 2500
    private const val REQUIRED_SHAKE_COUNT = 3

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var shakeTimestamp: Long = 0
    private var shakeCount: Int = 0
    private var lastTriggerTime: Long = 0

    private val _isShakeDetectionEnabled = MutableStateFlow(true)
    val isShakeDetectionEnabled: StateFlow<Boolean> = _isShakeDetectionEnabled.asStateFlow()

    fun setShakeDetectionEnabled(enabled: Boolean, context: Context) {
        _isShakeDetectionEnabled.value = enabled
        if (enabled) {
            register(context)
        } else {
            unregister()
        }
    }

    fun register(context: Context) {
        if (!_isShakeDetectionEnabled.value) return
        try {
            if (sensorManager == null) {
                sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
                accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            accelerometer?.let { sensor ->
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                Log.d(TAG, "ShakeDetector registered successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register ShakeDetector: ${e.message}")
        }
    }

    fun unregister() {
        try {
            sensorManager?.unregisterListener(this)
            Log.d(TAG, "ShakeDetector unregistered.")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering ShakeDetector: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isShakeDetectionEnabled.value) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // gForce will be close to 1 when there is no movement
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()

            // Ignore shake events too close to each other
            if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            // Reset the shake count if the last shake was too long ago
            if (shakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0
            }

            shakeTimestamp = now
            shakeCount++
            Log.d(TAG, "Shake detected! Count: $shakeCount / $REQUIRED_SHAKE_COUNT (gForce: $gForce)")

            if (shakeCount >= REQUIRED_SHAKE_COUNT) {
                shakeCount = 0
                // Debounce full SOS triggers to prevent duplicate rapid firings within 10 seconds
                if (now - lastTriggerTime > 10000) {
                    lastTriggerTime = now
                    onVigorousShakeDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        register(context)
    }

    private fun onVigorousShakeDetected() {
        val context = appContext ?: return
        Log.w(TAG, "🚨 VIGOROUS SHAKE DETECTED! Triggering Emergency SOS & Dispatching SMS!")

        // 1. Trigger haptic vibration feedback
        vibratePhone(context)

        // 2. Dispatch Emergency SMS with Fused GPS Location asynchronously in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
                EmergencySmsDispatcher.dispatchEmergencySms(
                    context = context,
                    customNote = "Triggered via 3x Emergency Shake",
                    location = loc
                )
                // Broadcast to Community Safety Network
                CommunityManager.broadcastSosAlert(
                    triggerTypeLabel = "3x Phone Shake Trigger",
                    location = loc,
                    note = "Emergency SOS triggered automatically by 3x vigorous phone shake gesture."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in shake SOS dispatch: ${e.message}")
            }
        }

        // 3. Launch MainActivity in Emergency Mode
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("EXTRA_OPEN_EMERGENCY", true)
                putExtra("EXTRA_TRIGGER_TYPE", "SHAKE_GESTURE")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Emergency Activity on shake: ${e.message}")
        }
    }

    private fun vibratePhone(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering vibration: ${e.message}")
        }
    }
}
