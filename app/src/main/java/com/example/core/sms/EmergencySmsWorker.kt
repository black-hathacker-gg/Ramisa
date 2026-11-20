package com.example.core.sms.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.sms.EmergencySmsDispatcher
import kotlinx.coroutines.CancellationException

/** Compatibility handler for older queued work. The first submission now happens in the foreground. */
class EmergencySmsWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val eventId = inputData.getString("event_id") ?: return Result.failure()
        return try {
            if (EmergencySmsDispatcher.restoreReport(applicationContext, eventId) != null)
                Result.success() else Result.failure()
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Result.failure() }
    }
}
