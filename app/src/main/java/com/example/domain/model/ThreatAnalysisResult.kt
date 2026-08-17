package com.example.domain.model

enum class ThreatLevel {
  LOW,
  MEDIUM,
  HIGH,
  CRITICAL
}

data class ThreatAnalysisResult(
  val threatLevel: ThreatLevel = ThreatLevel.LOW,
  val confidenceScore: Float = 0.92f,
  val decibelLevel: Int = 42,
  val detectedAnomalies: List<String> = emptyList(),
  val recommendedAction: String = "Environment appears calm and secure.",
  val recommendedActionBn: String = "পরিবেশ নিরাপদ ও শান্ত রয়েছে।",
  val timestamp: Long = System.currentTimeMillis()
)
