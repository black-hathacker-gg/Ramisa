package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val relationship: String,
  val priority: Int, // 1 to 7
  val smsEnabled: Boolean = true,
  val callEnabled: Boolean = true,
  val pushEnabled: Boolean = true,
  val isVerified: Boolean = true
)
