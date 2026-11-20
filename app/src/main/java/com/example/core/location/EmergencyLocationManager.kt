package com.example.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.data.local.AppDatabase
import com.example.data.local.LocationEntity
import com.example.domain.model.GeoLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Actual device fixes only. Unavailable, stale and approximate locations remain distinguishable. */
object EmergencyLocationManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _latestLocation = MutableStateFlow<GeoLocation?>(null)
    val latestLocation = _latestLocation.asStateFlow()
    private val _isGpsHardwareEnabled = MutableStateFlow(false)
    val isGpsHardwareEnabled = _isGpsHardwareEnabled.asStateFlow()
    private var nativeListener: LocationListener? = null
    private var fusedCallback: LocationCallback? = null

    fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasFineLocation(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun checkLocationProviderStatus(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val enabled = manager != null && runCatching { LocationManagerCompat.isLocationEnabled(manager) }.getOrDefault(false)
        _isGpsHardwareEnabled.value = enabled
        return enabled
    }

    private fun Location.toGeo(context: Context) = GeoLocation(
        latitude = latitude,
        longitude = longitude,
        addressName = if (hasFineLocation(context)) "Device location" else "Approximate device location",
        accuracyMeters = if (hasAccuracy()) accuracy else Float.NaN,
        timestamp = time.takeIf { it > 0L } ?: 0L,
        elapsedRealtimeNanos = elapsedRealtimeNanos,
        isApproximate = !hasFineLocation(context)
    )

    fun updateCachedLocation(context: Context, location: Location) {
        if (!hasLocationPermission(context) || !location.latitude.isFinite() || !location.longitude.isFinite() ||
            location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) return
        val fix = location.toGeo(context)
        val previous = _latestLocation.value
        // A slower provider must not replace a newer fix with old cached coordinates.
        if (previous != null && previous.elapsedRealtimeNanos > 0 && fix.elapsedRealtimeNanos > 0 &&
            fix.elapsedRealtimeNanos < previous.elapsedRealtimeNanos) return
        _latestLocation.value = fix
        scope.launch {
            runCatching {
                AppDatabase.getDatabase(context.applicationContext).locationDao().insertLocation(
                    LocationEntity(latitude = fix.latitude, longitude = fix.longitude,
                        addressName = fix.addressName, accuracyMeters = fix.accuracyMeters, timestamp = fix.timestamp)
                )
            }
        }
    }

    fun seedInitialLocation(context: Context) {
        if (!hasLocationPermission(context)) {
            _latestLocation.value = null
            return
        }
        checkLocationProviderStatus(context)
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val cached = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider -> runCatching { manager?.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.elapsedRealtimeNanos }
        cached?.let { updateCachedLocation(context, it) }
        runCatching {
            LocationServices.getFusedLocationProviderClient(context.applicationContext).lastLocation
                .addOnSuccessListener { it?.let { fix -> updateCachedLocation(context, fix) } }
        }
        // A persisted fix is explicitly cached; its original time is retained across launches.
        if (_latestLocation.value == null) scope.launch {
            val saved = runCatching { AppDatabase.getDatabase(context.applicationContext).locationDao().getLatestLocation() }.getOrNull()
            if (saved != null && _latestLocation.value == null && hasLocationPermission(context)) {
                _latestLocation.value = GeoLocation(saved.latitude, saved.longitude,
                    "Previously saved location", saved.accuracyMeters, saved.timestamp,
                    elapsedRealtimeNanos = 0L, isApproximate = !hasFineLocation(context))
            }
        }
    }

    fun startBackgroundLocationUpdates(context: Context) {
        if (!hasLocationPermission(context)) return
        seedInitialLocation(context)
        val app = context.applicationContext
        val manager = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (nativeListener == null && manager != null) {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) = updateCachedLocation(app, location)
                override fun onProviderEnabled(provider: String) { checkLocationProviderStatus(app) }
                override fun onProviderDisabled(provider: String) { checkLocationProviderStatus(app) }
                @Deprecated("Required on older Android versions")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }
            var registered = false
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
                if (runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)) {
                    runCatching { manager.requestLocationUpdates(provider, 3000L, 1f, listener, Looper.getMainLooper()) }
                        .onSuccess { registered = true }
                }
            }
            if (registered) nativeListener = listener
        }
        if (fusedCallback == null) {
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) { result.lastLocation?.let { updateCachedLocation(app, it) } }
            }
            runCatching {
                val priority = if (hasFineLocation(app)) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
                LocationServices.getFusedLocationProviderClient(app).requestLocationUpdates(
                    LocationRequest.Builder(priority, 3000L).setWaitForAccurateLocation(false).build(),
                    callback, Looper.getMainLooper())
            }.onSuccess { fusedCallback = callback }
        }
    }

    fun stopBackgroundLocationUpdates(context: Context) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        nativeListener?.let { runCatching { manager?.removeUpdates(it) } }
        nativeListener = null
        fusedCallback?.let { callback -> runCatching {
            LocationServices.getFusedLocationProviderClient(context.applicationContext).removeLocationUpdates(callback)
        } }
        fusedCallback = null
    }

    suspend fun getAccurateEmergencyCoordinates(context: Context): GeoLocation = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        if (!hasLocationPermission(app)) return@withContext GeoLocation(addressName = "Location permission not granted")
        seedInitialLocation(app)
        _latestLocation.value?.takeIf { it.isAvailable && (it.locationAgeMillis ?: Long.MAX_VALUE) < 30_000L }?.let { return@withContext it }
        val manager = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager != null && checkLocationProviderStatus(app)) {
            val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).firstOrNull {
                (it != LocationManager.GPS_PROVIDER || hasFineLocation(app)) &&
                    runCatching { manager.isProviderEnabled(it) }.getOrDefault(false)
            }
            if (provider != null) {
                val fresh = withTimeoutOrNull(6000L) { currentNativeFix(app, manager, provider) }
                if (fresh != null) {
                    updateCachedLocation(app, fresh)
                    return@withContext fresh.toGeo(app)
                }
            }
        }
        // Sending an SOS must not wait forever for GPS or silently fabricate an address.
        _latestLocation.value?.takeIf { it.isAvailable }
            ?: GeoLocation(addressName = "Location unavailable: no device fix received")
    }

    private suspend fun currentNativeFix(context: Context, manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationSignal()
            continuation.invokeOnCancellation { cancellation.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(manager, provider, cancellation,
                    ContextCompat.getMainExecutor(context)) { fix ->
                    if (continuation.isActive) continuation.resume(fix)
                }
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
}
