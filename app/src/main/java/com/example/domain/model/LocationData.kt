package com.example.domain.model

data class GeoLocation(
  val latitude: Double = 23.7937,
  val longitude: Double = 90.4066,
  val addressName: String = "Banani, Dhaka, Bangladesh",
  val accuracyMeters: Float = 4.5f,
  val timestamp: Long = System.currentTimeMillis()
)

data class AlertDispatchResult(
  val alertId: String,
  val smsSentCount: Int,
  val pushDeliveredCount: Int,
  val nationalHotlineDialed: Boolean,
  val locationSharedUrl: String,
  val isOfflineQueued: Boolean = false,
  val dispatchedAt: Long = System.currentTimeMillis()
)
