package com.example.domain.model

data class EmergencyContact(
  val id: String,
  val name: String,
  val phone: String,
  val relationship: String,
  val priority: Int, // 1 to 7
  val smsEnabled: Boolean = true,
  val callEnabled: Boolean = true,
  val pushEnabled: Boolean = true,
  val isVerified: Boolean = true
)
