package com.example.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.R
import com.example.core.sensor.ShakeDetectorManager
import com.example.features.battery.BatteryMonitorManager

class RamisaSafetyService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createPersistentNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }

        // Register Battery and Shake Detector monitoring
        try {
            BatteryMonitorManager.registerReceiver(this)
            ShakeDetectorManager.initialize(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize sensor receivers: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            BatteryMonitorManager.unregisterReceiver(this)
            ShakeDetectorManager.unregister()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister receivers: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RAMISA Active Safety Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs in the background to monitor emergency triggers, shake sensor, and battery levels."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createPersistentNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RAMISA Safety Guard")
            .setContentText("Actively monitoring 3x Shake SOS and battery triggers.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "RamisaSafetyService"
        const val CHANNEL_ID = "ramisa_safety_service_channel"
        const val NOTIFICATION_ID = 8001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, RamisaSafetyService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to start RamisaSafetyService: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, RamisaSafetyService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to stop RamisaSafetyService: ${e.message}")
            }
        }
    }
}
