package com.example.core.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Explicit PendingIntent destination; declare android:exported="false". */
class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val code = resultCode
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { EmergencySmsDispatcher.recordReceipt(context.applicationContext, intent, code) }
            catch (_: Exception) { /* A malformed/unpersisted receipt cannot confirm delivery. */ }
            finally { pending.finish() }
        }
    }
}
