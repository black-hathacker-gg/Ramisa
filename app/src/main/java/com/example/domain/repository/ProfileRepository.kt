package com.example.domain.repository

import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
  val profile: StateFlow<UserProfile>

  suspend fun updateProfile(
    name: String,
    phone: String,
    ageCategory: AgeCategory,
    userType: UserType,
    bloodGroup: String,
    emergencyNote: String,
    guardianName: String?,
    guardianPhone: String?
  ): Result<UserProfile>

  suspend fun updateEmergencyPin(oldPin: String, newPin: String): Result<Boolean>
  suspend fun toggleChildMode(enabled: Boolean, guardianPin: String): Result<Boolean>
}
