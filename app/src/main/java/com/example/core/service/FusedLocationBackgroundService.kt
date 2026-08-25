package com.example.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.location.EmergencyLocationManager

/**
 * Foreground Service utilizing FusedLocationProviderClient to track device coordinates
 * in the background for real-time inclusion in emergency SOS SMS dispatches.
 */
class FusedLocationBackgroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createServiceNotification("RAMISA GPS Guard Active", "Tracking high-accuracy coordinates for Emergency SMS.")
        startForeground(NOTIFICATION_ID, notification)

        // Start high accuracy location tracking via FusedLocationProviderClient
        EmergencyLocationManager.startBackgroundLocationUpdates(this)
        Log.d(TAG, "FusedLocationBackgroundService created and tracking started.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_LOCATION_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_REFRESH_LOCATION -> {
                EmergencyLocationManager.startBackgroundLocationUpdates(this)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        EmergencyLocationManager.stopBackgroundLocationUpdates(this)
        Log.d(TAG, "FusedLocationBackgroundService stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "RAMISA GPS Emergency Tracking"
            val descriptionText = "Continuous background location tracking for instant SOS SMS alerts."
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createServiceNotification(title: String, content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "FusedLocationService"
        const val CHANNEL_ID = "ramisa_fused_location_channel"
        const val NOTIFICATION_ID = 8002

        const val ACTION_START_LOCATION_SERVICE = "com.example.ACTION_START_LOCATION_SERVICE"
        const val ACTION_STOP_LOCATION_SERVICE = "com.example.ACTION_STOP_LOCATION_SERVICE"
        const val ACTION_REFRESH_LOCATION = "com.example.ACTION_REFRESH_LOCATION"

        fun startLocationService(context: Context) {
            val intent = Intent(context, FusedLocationBackgroundService::class.java).apply {
                action = ACTION_START_LOCATION_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopLocationService(context: Context) {
            val intent = Intent(context, FusedLocationBackgroundService::class.java).apply {
                action = ACTION_STOP_LOCATION_SERVICE
            }
            context.startService(intent)
        }
    }
}
