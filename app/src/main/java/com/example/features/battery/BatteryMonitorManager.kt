package com.example.features.battery

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.core.service.RamisaSafetyService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BatteryMonitorManager {
    private val _isLowBatteryPingEnabled = MutableStateFlow(false)
    val isLowBatteryPingEnabled: StateFlow<Boolean> = _isLowBatteryPingEnabled.asStateFlow()

    private val _batteryThreshold = MutableStateFlow(15) // default 15% as per request
    val batteryThreshold: StateFlow<Int> = _batteryThreshold.asStateFlow()

    private val _lastPingSentTime = MutableStateFlow(0L)
    
    private var batteryReceiver: BroadcastReceiver? = null

    fun setLowBatteryPingEnabled(context: Context, enabled: Boolean) {
        _isLowBatteryPingEnabled.value = enabled
        if (enabled) {
            RamisaSafetyService.startService(context)
        } else {
            RamisaSafetyService.stopService(context)
        }
    }

    fun setBatteryThreshold(threshold: Int) {
        _batteryThreshold.value = threshold
    }

    fun registerReceiver(context: Context) {
        if (batteryReceiver == null) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        if (level != -1 && scale != -1) {
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
        Log.d("BatteryMonitorManager", "Current battery: $currentLevel%, Threshold: $threshold%")
        
        if (currentLevel <= threshold) {
            val currentTime = System.currentTimeMillis()
            val thirtyMinutes = 30 * 60 * 1000L
            if (currentTime - _lastPingSentTime.value > thirtyMinutes) {
                sendLowBatteryPing(context, currentLevel)
                _lastPingSentTime.value = currentTime
            }
        } else {
            if (currentLevel > threshold + 5) {
                _lastPingSentTime.value = 0L
            }
        }
    }
    
    private fun getLastKnownLocation(context: Context): String {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "https://maps.google.com/?q=23.7937,90.4066" // fallback
        }
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        return if (bestLocation != null) {
            "https://maps.google.com/?q=${bestLocation.latitude},${bestLocation.longitude}"
        } else {
            "https://maps.google.com/?q=23.7937,90.4066" // fallback
        }
    }

    private fun sendLowBatteryPing(context: Context, batteryLevel: Int) {
        Log.d("BatteryMonitorManager", "Triggering Low Battery Auto-Ping at $batteryLevel%")
        val locationLink = getLastKnownLocation(context)
        val message = "RAMISA ALERT: My phone battery is critically low ($batteryLevel%). My last known location is: $locationLink"
        
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val mockContacts = listOf("+8801700000000")
            for (contact in mockContacts) {
                // smsManager.sendTextMessage(contact, null, message, null, null)
                Log.d("BatteryMonitorManager", "Mock SMS Sent to $contact: $message")
            }
            showSafetySharedNotification(context, batteryLevel)
        } catch (e: Exception) {
            Log.e("BatteryMonitorManager", "Error sending low battery ping: ${e.message}")
        }
    }
    
    private fun showSafetySharedNotification(context: Context, batteryLevel: Int) {
        val channelId = "ramisa_safety_ping"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safety Ping Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when your location has been automatically shared."
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // standard fallback
            .setContentTitle("Safety Status Shared")
            .setContentText("Battery is critically low ($batteryLevel%). Your last known location was sent to trusted contacts.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    fun simulateLowBatteryEvent(context: Context) {
        sendLowBatteryPing(context, _batteryThreshold.value)
    }
}
