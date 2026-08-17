package com.example.domain.model

data class AreaSafetyReport(
  val id: String,
  val areaName: String,
  val areaNameBn: String,
  val district: String = "Dhaka",
  val safetyScore: Int, // 1 - 100
  val lightingRating: Float, // 1.0 - 5.0
  val policePatrolRating: Float, // 1.0 - 5.0
  val crowdDensity: String, // "High", "Moderate", "Isolated"
  val crowdDensityBn: String,
  val totalReviews: Int,
  val lastReportedMinutesAgo: Int,
  val isCautionZone: Boolean = false,
  val safetyTips: List<String> = emptyList(),
  val safetyTipsBn: List<String> = emptyList()
)

data class LiveShareSession(
  val trackingCode: String,
  val shareableWebUrl: String,
  val isSessionActive: Boolean = true,
  val expiresAtTimestamp: Long,
  val lastKnownLatitude: Double = 23.7258,
  val lastKnownLongitude: Double = 90.3976,
  val batteryLevel: Int = 88
)
