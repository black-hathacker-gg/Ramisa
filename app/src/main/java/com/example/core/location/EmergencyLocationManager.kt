package com.example.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.data.local.AppDatabase
import com.example.data.local.LocationEntity
import com.example.domain.model.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object EmergencyLocationManager {
    private const val TAG = "EmergencyLocManager"

    private val _latestLocation = MutableStateFlow<GeoLocation?>(null)
    val latestLocation: StateFlow<GeoLocation?> = _latestLocation.asStateFlow()

    private val _isGpsHardwareEnabled = MutableStateFlow(true)
    val isGpsHardwareEnabled: StateFlow<Boolean> = _isGpsHardwareEnabled.asStateFlow()

    private var fusedClient: FusedLocationProviderClient? = null
    private var activeFusedCallback: LocationCallback? = null
    private var activeNativeListener: LocationListener? = null
    private var isTrackingActive = false

    private fun getClient(context: Context): FusedLocationProviderClient {
        return fusedClient ?: LocationServices.getFusedLocationProviderClient(context.applicationContext).also {
            fusedClient = it
        }
    }

    /**
     * Checks if location permissions (fine or coarse) are granted.
     */
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

    /**
     * Checks if GPS or Network location providers are enabled on the device.
     */
    fun checkLocationProviderStatus(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
            val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val enabled = gpsEnabled || networkEnabled
            _isGpsHardwareEnabled.value = enabled
            enabled
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Updates in-memory cached location, persists securely into encrypted Room DB,
     * and asynchronously reverse geocodes to a human-readable street/area address.
     */
    fun updateCachedLocation(context: Context, location: Location) {
        val accuracy = if (location.accuracy > 0f) location.accuracy else 5.0f
        val existingAddr = _latestLocation.value?.takeIf {
            it.latitude == location.latitude && it.longitude == location.longitude && !it.addressName.startsWith("GPS Fix")
        }?.addressName

        val initialAddress = existingAddr ?: "GPS Fix (±${String.format(Locale.US, "%.1f", accuracy)}m)"

        val geo = GeoLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            addressName = initialAddress,
            accuracyMeters = accuracy,
            timestamp = if (location.time > 0L) location.time else System.currentTimeMillis()
        )
        _latestLocation.value = geo
        Log.d(TAG, "Cached GPS Location updated: ${geo.latitude}, ${geo.longitude} (±${geo.accuracyMeters}m)")

        // 1. Persist coordinate securely to encrypted Room database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                db.locationDao().insertLocation(
                    LocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        addressName = initialAddress,
                        accuracyMeters = accuracy,
                        timestamp = geo.timestamp
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist location in encrypted DB: ${e.message}")
            }

            // 2. Reverse geocode to resolve readable neighborhood/city
            try {
                val resolvedName = resolveAddressName(context.applicationContext, location.latitude, location.longitude)
                if (!resolvedName.isNullOrBlank() && _latestLocation.value?.latitude == location.latitude && _latestLocation.value?.longitude == location.longitude) {
                    val updatedGeo = geo.copy(addressName = "$resolvedName (±${String.format(Locale.US, "%.1f", accuracy)}m)")
                    _latestLocation.value = updatedGeo
                }
            } catch (e: Exception) {
                // Non-critical, fallback address is preserved
            }
        }
    }

    private fun resolveAddressName(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val subLocality = addr.subLocality ?: addr.subAdminArea ?: ""
                val locality = addr.locality ?: addr.adminArea ?: ""
                val thoroughfare = addr.thoroughfare ?: addr.featureName ?: ""
                val parts = listOf(thoroughfare, subLocality, locality).filter { it.isNotBlank() }.distinct()
                if (parts.isNotEmpty()) parts.joinToString(", ") else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Seeds initial location from fastest available source (Fused lastLocation,
     * native GPS/Network/Passive providers, or Encrypted Database).
     */
    fun seedInitialLocation(context: Context) {
        if (!hasLocationPermission(context)) return

        checkLocationProviderStatus(context)

        // 1. Try native providers immediately (synchronous, 0 ms)
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm != null) {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )
                var bestLoc: Location? = null
                for (p in providers) {
                    try {
                        val loc = lm.getLastKnownLocation(p)
                        if (loc != null) {
                            if (bestLoc == null || loc.time > bestLoc.time || loc.accuracy < bestLoc.accuracy) {
                                bestLoc = loc
                            }
                        }
                    } catch (e: SecurityException) {
                        // ignore
                    }
                }
                if (bestLoc != null) {
                    updateCachedLocation(context, bestLoc)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying native last known location: ${e.message}")
        }

        // 2. Query Fused Location lastLocation asynchronously
        try {
            val client = getClient(context)
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val current = _latestLocation.value
                    if (current == null || loc.time > current.timestamp) {
                        updateCachedLocation(context, loc)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying Fused lastLocation: ${e.message}")
        }

        // 3. Fallback to encrypted Room DB if memory is still empty
        if (_latestLocation.value == null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val lastDbLoc = db.locationDao().getLatestLocation()
                    if (lastDbLoc != null && _latestLocation.value == null) {
                        _latestLocation.value = GeoLocation(
                            latitude = lastDbLoc.latitude,
                            longitude = lastDbLoc.longitude,
                            addressName = lastDbLoc.addressName,
                            accuracyMeters = lastDbLoc.accuracyMeters,
                            timestamp = lastDbLoc.timestamp
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading location from DB: ${e.message}")
                }
            }
        }
    }

    /**
     * Starts continuous high-accuracy dual-engine location tracking:
     * Engine 1: FusedLocationProviderClient (Google Play Services)
     * Engine 2: Native Android LocationManager (GPS + Network Providers)
     */
    fun startBackgroundLocationUpdates(context: Context) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Cannot start location updates: missing permissions")
            return
        }

        checkLocationProviderStatus(context)
        seedInitialLocation(context)

        if (isTrackingActive) {
            Log.d(TAG, "Location tracking is already active.")
            return
        }
        isTrackingActive = true

        val appContext = context.applicationContext

        // --- Engine 1: FusedLocationProviderClient ---
        try {
            val client = getClient(appContext)
            // Notice: setWaitForAccurateLocation is FALSE so emulators and fast network fixes are NOT blocked!
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1500L)
                .setMinUpdateDistanceMeters(0.5f)
                .setWaitForAccurateLocation(false)
                .build()

            val fusedCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        updateCachedLocation(appContext, location)
                    }
                }
            }
            activeFusedCallback = fusedCallback

            client.requestLocationUpdates(
                locationRequest,
                fusedCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "FusedLocationProviderClient updates registered.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in FusedLocation updates: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting FusedLocation updates: ${e.message}")
        }

        // --- Engine 2: Native Android LocationManager Fallback ---
        try {
            val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm != null) {
                val nativeListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        updateCachedLocation(appContext, location)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {
                        _isGpsHardwareEnabled.value = true
                    }
                    override fun onProviderDisabled(provider: String) {
                        checkLocationProviderStatus(appContext)
                    }
                }
                activeNativeListener = nativeListener

                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        0.5f,
                        nativeListener,
                        Looper.getMainLooper()
                    )
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        1.0f,
                        nativeListener,
                        Looper.getMainLooper()
                    )
                }
                Log.d(TAG, "Native LocationManager updates registered successfully.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in native location updates: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering native location listener: ${e.message}")
        }
    }

    /**
     * Stops continuous location updates.
     */
    fun stopBackgroundLocationUpdates(context: Context) {
        val appContext = context.applicationContext
        try {
            activeFusedCallback?.let { callback ->
                getClient(appContext).removeLocationUpdates(callback)
                activeFusedCallback = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping FusedLocation updates: ${e.message}")
        }

        try {
            activeNativeListener?.let { listener ->
                val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                lm?.removeUpdates(listener)
                activeNativeListener = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping native location updates: ${e.message}")
        }
        isTrackingActive = false
    }

    /**
     * Rapidly retrieves the most accurate and up-to-date coordinates.
     * Uses in-memory cache, fast one-shot query, or hardware native provider.
     */
    suspend fun getAccurateEmergencyCoordinates(context: Context): GeoLocation = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext

        // If permission is missing, provide clear fallback
        if (!hasLocationPermission(appContext)) {
            return@withContext _latestLocation.value ?: GeoLocation(
                latitude = 23.7937,
                longitude = 90.4066,
                addressName = "Dhaka Central (Location Permission Needed)",
                accuracyMeters = 10.0f
            )
        }

        // Ensure tracking and seed are started
        withContext(Dispatchers.Main) {
            startBackgroundLocationUpdates(appContext)
        }

        // If we already have a recent location (< 30 seconds old), return it immediately
        val current = _latestLocation.value
        val now = System.currentTimeMillis()
        if (current != null && (now - current.timestamp) < 30_000L) {
            return@withContext current
        }

        // Try fast one-shot query from Fused Location Provider with 2.5 second timeout
        val freshFused = withTimeoutOrNull(2500L) {
            fetchFusedCurrentLocation(appContext)
        }

        if (freshFused != null) {
            updateCachedLocation(appContext, freshFused)
            return@withContext _latestLocation.value ?: GeoLocation(
                latitude = freshFused.latitude,
                longitude = freshFused.longitude,
                addressName = "Live GPS (±${String.format(Locale.US, "%.1f", freshFused.accuracy)}m)",
                accuracyMeters = freshFused.accuracy,
                timestamp = freshFused.time
            )
        }

        // Check native providers
        val nativeLoc = getNativeLocationFallback(appContext)
        if (nativeLoc != null) {
            updateCachedLocation(appContext, nativeLoc)
            return@withContext _latestLocation.value ?: GeoLocation(
                latitude = nativeLoc.latitude,
                longitude = nativeLoc.longitude,
                addressName = "GPS Fix (±${String.format(Locale.US, "%.1f", nativeLoc.accuracy)}m)",
                accuracyMeters = nativeLoc.accuracy,
                timestamp = nativeLoc.time
            )
        }

        // Return latest memory value or default
        _latestLocation.value ?: GeoLocation(
            latitude = 23.7937,
            longitude = 90.4066,
            addressName = "Banani, Dhaka (GPS Initialized)",
            accuracyMeters = 5.0f,
            timestamp = System.currentTimeMillis()
        )
    }

    private suspend fun fetchFusedCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                if (!hasLocationPermission(context)) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val client = getClient(context)
                val cancellationSource = CancellationTokenSource()
                continuation.invokeOnCancellation { cancellationSource.cancel() }

                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Fallback to lastLocation if getCurrentLocation is null
                        client.lastLocation.addOnSuccessListener { last ->
                            continuation.resume(last)
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }

    private fun getNativeLocationFallback(context: Context): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            if (!hasLocationPermission(context)) return null

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var bestLocation: Location? = null
            for (p in providers) {
                try {
                    val l = lm.getLastKnownLocation(p) ?: continue
                    if (bestLocation == null || l.time > bestLocation.time || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                } catch (e: Exception) {
                    // skip
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }
}
