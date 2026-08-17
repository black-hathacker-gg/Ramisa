package com.example.domain.model

enum class EmergencyStatus {
  IDLE,
  WARNING,
  SOS_ACTIVE,
  CONTACTS_NOTIFIED,
  WAITING_FOR_NETWORK,
  RESOLVED
}

enum class TriggerType {
  SOS_BUTTON,
  JOURNEY_DEVIATION,
  SAFETY_CHECK_TIMEOUT,
  WEARABLE,
  TEST_SOS
}

data class EmergencyEvent(
  val id: String,
  val triggerType: TriggerType,
  val timestamp: Long = System.currentTimeMillis(),
  val locationLabel: String = "Dhaka, Bangladesh (GPS Pending)",
  val status: EmergencyStatus = EmergencyStatus.SOS_ACTIVE,
  val notes: String = "",
  val batteryLevel: Int = 85
)
