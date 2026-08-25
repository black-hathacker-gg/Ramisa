package com.example.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.features.lockscreen.LockScreenSosManager

class RamisaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each of the widgets with the remote adapter
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_ramisa_safety)

        // 1. SOS Button Intent (Trigger LockScreenSosReceiver which handles offline SOS)
        val sosIntent = Intent(context, com.example.features.lockscreen.LockScreenSosReceiver::class.java).apply {
            action = LockScreenSosManager.ACTION_TRIGGER_LOCKSCREEN_SOS
        }
        val sosPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            sosIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_sos, sosPendingIntent)

        // 2. Fake Call Intent (Launch MainActivity with flag)
        val fakeCallIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_OPEN_FAKE_CALL", true)
        }
        val fakeCallPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fakeCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_fake_call, fakeCallPendingIntent)

        // 3. Safe Journey Intent
        val safeJourneyIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_OPEN_SAFE_JOURNEY", true)
        }
        val safeJourneyPendingIntent = PendingIntent.getActivity(
            context,
            3,
            safeJourneyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_safe_journey, safeJourneyPendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
