package com.example.domain.model

data class GeoLocation(
  val latitude: Double = Double.NaN,
  val longitude: Double = Double.NaN,
  val addressName: String = "Location unavailable",
  val accuracyMeters: Float = Float.NaN,
  val timestamp: Long = 0L,
  val elapsedRealtimeNanos: Long = 0L,
  val isApproximate: Boolean = false
) {
  val isAvailable: Boolean
    get() = latitude.isFinite() && longitude.isFinite() &&
      latitude in -90.0..90.0 && longitude in -180.0..180.0

  val locationAgeMillis: Long?
    get() = ageMillis(android.os.SystemClock.elapsedRealtimeNanos(), System.currentTimeMillis())

  fun ageMillis(nowElapsedRealtimeNanos: Long, nowWallTimeMillis: Long): Long? {
    if (!isAvailable || timestamp <= 0L) return null
    if (elapsedRealtimeNanos > 0L && nowElapsedRealtimeNanos >= elapsedRealtimeNanos) {
      return (nowElapsedRealtimeNanos - elapsedRealtimeNanos) / 1_000_000L
    }
    return (nowWallTimeMillis - timestamp).coerceAtLeast(0L)
  }

  fun description(): String {
    if (!isAvailable) return addressName.ifBlank { "Location unavailable" }
    val accuracy = if (accuracyMeters.isFinite() && accuracyMeters >= 0f)
      "accuracy ±${accuracyMeters.toInt()} m" else "accuracy unknown"
    val age = locationAgeMillis?.let { "${it / 1000L} s old" } ?: "age unknown"
    return "$addressName • $accuracy • $age${if (isApproximate) " • approximate" else ""}"
  }
}

data class AlertDispatchResult(
  val alertId: String,
  val smsSentCount: Int,
  val pushDeliveredCount: Int,
  val nationalHotlineDialed: Boolean,
  val locationSharedUrl: String,
  val isOfflineQueued: Boolean = false,
  val dispatchedAt: Long = System.currentTimeMillis()
)
