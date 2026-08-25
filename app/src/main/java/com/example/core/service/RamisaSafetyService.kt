package com.example.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.core.sensor.ShakeDetectorManager
import com.example.features.battery.BatteryMonitorManager

class RamisaSafetyService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createPersistentNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Register Battery and Shake Detector monitoring
        BatteryMonitorManager.registerReceiver(this)
        ShakeDetectorManager.initialize(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        BatteryMonitorManager.unregisterReceiver(this)
        ShakeDetectorManager.unregister()
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
            .setContentText("Actively monitoring 3x Shake SOS, GPS coordinates, and battery triggers.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "ramisa_safety_service_channel"
        const val NOTIFICATION_ID = 8001

        fun startService(context: Context) {
            val intent = Intent(context, RamisaSafetyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RamisaSafetyService::class.java)
            context.stopService(intent)
        }
    }
}
