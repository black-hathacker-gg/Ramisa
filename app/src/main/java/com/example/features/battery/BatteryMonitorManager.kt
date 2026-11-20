package com.example.features.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.example.core.service.RamisaSafetyService
import com.example.core.sms.EmergencySmsDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BatteryMonitorManager {
    private val _isLowBatteryPingEnabled = MutableStateFlow(false)
    val isLowBatteryPingEnabled: StateFlow<Boolean> = _isLowBatteryPingEnabled.asStateFlow()

    private val _batteryThreshold = MutableStateFlow(15) // default 15% as per request
    val batteryThreshold: StateFlow<Int> = _batteryThreshold.asStateFlow()

    private const val PREFS = "battery_safety"
    private const val ENABLED = "enabled"
    private const val THRESHOLD = "threshold"
    private const val PING_ATTEMPTED = "ping_attempted"
    private var initialized = false
    private var batteryReceiver: BroadcastReceiver? = null

    fun initialize(context: Context) {
        if (initialized) return
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _isLowBatteryPingEnabled.value = preferences.getBoolean(ENABLED, false)
        _batteryThreshold.value = preferences.getInt(THRESHOLD, 15)
        initialized = true
    }

    fun setLowBatteryPingEnabled(context: Context, enabled: Boolean) {
        initialize(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ENABLED, enabled).apply()
        _isLowBatteryPingEnabled.value = enabled
        if (enabled) RamisaSafetyService.startService(context)
    }

    fun setBatteryThreshold(context: Context, threshold: Int) {
        initialize(context)
        val level = threshold.coerceIn(5, 20)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(THRESHOLD, level).apply()
        _batteryThreshold.value = level
    }

    fun registerReceiver(context: Context) {
        initialize(context)
        if (batteryReceiver == null) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        if (level >= 0 && scale > 0) {
                            val batteryPct = (level * 100) / scale.toFloat()
                            checkBatteryLevel(context, batteryPct.toInt())
                        }
                    }
                }
            }
            context.applicationContext.registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            Log.d("BatteryMonitorManager", "Battery receiver registered.")
        }
    }

    fun unregisterReceiver(context: Context) {
        batteryReceiver?.let {
            try {
                context.applicationContext.unregisterReceiver(it)
                Log.d("BatteryMonitorManager", "Battery receiver unregistered.")
            } catch (e: Exception) {
                Log.e("BatteryMonitorManager", "Error unregistering receiver: ${e.message}")
            }
            batteryReceiver = null
        }
    }

    private fun checkBatteryLevel(context: Context, currentLevel: Int) {
        val threshold = _batteryThreshold.value
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (currentLevel > threshold + 5) {
            if (preferences.getBoolean(PING_ATTEMPTED, false)) {
                preferences.edit().remove(PING_ATTEMPTED).apply()
            }
            return
        }
        if (!_isLowBatteryPingEnabled.value || currentLevel > threshold ||
            preferences.getBoolean(PING_ATTEMPTED, false)) return
        Log.d("BatteryMonitorManager", "Current battery: $currentLevel%, Threshold: $threshold%")
        preferences.edit().putBoolean(PING_ATTEMPTED, true).apply()
        sendLowBatteryPing(context, currentLevel)
    }
    
    private fun sendLowBatteryPing(context: Context, batteryLevel: Int) {
        Log.d("BatteryMonitorManager", "Triggering Immediate Low Battery Auto-Ping at $batteryLevel%")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                EmergencySmsDispatcher.dispatchEmergencySms(
                    context = context,
                    customNote = "CRITICAL BATTERY ALERT: $batteryLevel%"
                )
                // Receipt-backed global status reports the result; submission alone is not delivery.
            } catch (e: Exception) {
                Log.e("BatteryMonitorManager", "Error dispatching low battery emergency SMS: ${e.message}")
            }
        }
    }
    
}
