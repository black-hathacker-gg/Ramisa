package com.example.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.domain.model.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object EmergencyLocationManager {
    private const val TAG = "EmergencyLocManager"

    private val _latestLocation = MutableStateFlow<GeoLocation?>(null)
    val latestLocation: StateFlow<GeoLocation?> = _latestLocation.asStateFlow()

    private var fusedClient: FusedLocationProviderClient? = null
    private var activeLocationCallback: LocationCallback? = null

    private fun getClient(context: Context): FusedLocationProviderClient {
        return fusedClient ?: LocationServices.getFusedLocationProviderClient(context.applicationContext).also {
            fusedClient = it
        }
    }

    fun updateCachedLocation(location: Location) {
        val geo = GeoLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            addressName = "GPS Fix (±${String.format("%.1f", location.accuracy)}m)",
            accuracyMeters = location.accuracy
        )
        _latestLocation.value = geo
        Log.d(TAG, "Cached GPS Location updated: ${geo.latitude}, ${geo.longitude} (±${geo.accuracyMeters}m)")
    }

    /**
     * Starts continuous high-accuracy background location updates via FusedLocationProviderClient.
     */
    fun startBackgroundLocationUpdates(context: Context) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Cannot start FusedLocation updates: missing permissions")
            return
        }

        try {
            val client = getClient(context)
            stopBackgroundLocationUpdates(context)

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .setMinUpdateDistanceMeters(1.0f)
                .setWaitForAccurateLocation(true)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        updateCachedLocation(location)
                    }
                }
            }
            activeLocationCallback = callback

            client.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "FusedLocationProviderClient background updates started successfully.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in startBackgroundLocationUpdates: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting FusedLocation updates: ${e.message}")
        }
    }

    /**
     * Stops continuous background location updates.
     */
    fun stopBackgroundLocationUpdates(context: Context) {
        try {
            activeLocationCallback?.let { callback ->
                getClient(context).removeLocationUpdates(callback)
                activeLocationCallback = null
                Log.d(TAG, "FusedLocationProviderClient updates stopped.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates: ${e.message}")
        }
    }

    /**
     * Asynchronously retrieves the most accurate and up-to-date coordinates using FusedLocationProviderClient.
     * Uses priority high accuracy with a 5-second timeout and fallback to cached/hardware providers.
     */
    suspend fun getAccurateEmergencyCoordinates(context: Context): GeoLocation = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext _latestLocation.value ?: GeoLocation(
                latitude = 23.7937,
                longitude = 90.4066,
                addressName = "Fallback GPS (Permission Needed)",
                accuracyMeters = 10.0f
            )
        }

        // 1. Try one-shot high accuracy via FusedLocationProviderClient
        val fusedLoc = withTimeoutOrNull(4500L) {
            fetchFusedCurrentLocation(context)
        }

        if (fusedLoc != null) {
            updateCachedLocation(fusedLoc)
            return@withContext GeoLocation(
                latitude = fusedLoc.latitude,
                longitude = fusedLoc.longitude,
                addressName = "Live Fused GPS (±${String.format("%.1f", fusedLoc.accuracy)}m)",
                accuracyMeters = fusedLoc.accuracy
            )
        }

        // 2. Try Fused LastLocation
        val lastFused = fetchFusedLastLocation(context)
        if (lastFused != null) {
            updateCachedLocation(lastFused)
            return@withContext GeoLocation(
                latitude = lastFused.latitude,
                longitude = lastFused.longitude,
                addressName = "Last Known Fused GPS (±${String.format("%.1f", lastFused.accuracy)}m)",
                accuracyMeters = lastFused.accuracy
            )
        }

        // 3. Fallback to Android native LocationManager
        val nativeLoc = getNativeLocationFallback(context)
        if (nativeLoc != null) {
            updateCachedLocation(nativeLoc)
            return@withContext GeoLocation(
                latitude = nativeLoc.latitude,
                longitude = nativeLoc.longitude,
                addressName = "Native GPS Fix (±${String.format("%.1f", nativeLoc.accuracy)}m)",
                accuracyMeters = nativeLoc.accuracy
            )
        }

        // 4. Return cached or default Dhaka coordinates
        _latestLocation.value ?: GeoLocation(
            latitude = 23.7937,
            longitude = 90.4066,
            addressName = "Emergency Coordinates (Dhaka)",
            accuracyMeters = 5.0f
        )
    }

    private suspend fun fetchFusedCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                val client = getClient(context)
                val cancellationSource = CancellationTokenSource()
                continuation.invokeOnCancellation { cancellationSource.cancel() }

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    private suspend fun fetchFusedLastLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                val client = getClient(context)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                client.lastLocation
                    .addOnSuccessListener { location -> continuation.resume(location) }
                    .addOnFailureListener { continuation.resume(null) }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    private fun getNativeLocationFallback(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
