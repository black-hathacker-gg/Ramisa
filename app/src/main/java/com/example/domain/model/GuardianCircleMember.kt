package com.example.domain.model

enum class CircleMemberStatus(val label: String, val badgeColorHex: Long) {
  SAFE("Safe at Destination", 0xFFE91E63),
  IN_TRANSIT("In Transit (Rickshaw/Bus)", 0xFFD81B60),
  LOW_BATTERY("Low Battery Alert", 0xFFAD1457),
  CHECKIN_DUE("Check-In Overdue", 0xFFC2185B),
  SOS_TRIGGERED("SOS Alert Active", 0xFF880E4F)
}

data class GuardianCircleMember(
  val id: String,
  val name: String,
  val relation: String,
  val phone: String,
  val status: CircleMemberStatus,
  val batteryLevel: Int,
  val lastLocation: String,
  val lastUpdatedTime: String,
  val isCheckinRequested: Boolean = false
)
