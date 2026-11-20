package com.example.core.sms.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.sms.EmergencySmsDispatcher
import com.example.domain.model.GeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySmsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val customNote = inputData.getString("custom_note")
            val lat = inputData.getDouble("lat", 0.0)
            val lng = inputData.getDouble("lng", 0.0)
            val location = if (lat != 0.0 || lng != 0.0) GeoLocation(lat, lng) else null

            EmergencySmsDispatcher.dispatchEmergencySms(applicationContext, customNote, location)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
