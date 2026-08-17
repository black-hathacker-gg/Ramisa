package com.example.domain.model

enum class RiskLevel {
  CRITICAL_DANGER, // High reported cases of sexual harassment / assault
  HIGH_RISK,       // Isolated spots, poor streetlighting, frequent harassment reports
  MODERATE_CAUTION // Overcrowded transit hubs, pickpocketing/eve-teasing
}

enum class DangerCategory {
  SEXUAL_HARASSMENT_HOTSPOT,
  ISOLATED_DARK_CORRIDOR,
  HIGH_CRIME_TRANSIT_HUB,
  UNMONITORED_PARK_ALLEY
}

data class DangerZone(
  val id: String,
  val name: String,
  val areaName: String,
  val district: String = "Dhaka",
  val riskLevel: RiskLevel,
  val category: DangerCategory,
  val latitude: Double,
  val longitude: Double,
  val radiusMeters: Int,
  val reportedIncidentsSummary: String,
  val incidentStats: String,
  val peakVulnerableHours: String,
  val nearestPoliceStation: String,
  val policeContact: String,
  val safetyAdvisory: String,
  val safeAlternativeRoute: String,
  val isLightingPoor: Boolean = true,
  val hasCctvCoverage: Boolean = false,
  val userReportsCount: Int = 142
)
