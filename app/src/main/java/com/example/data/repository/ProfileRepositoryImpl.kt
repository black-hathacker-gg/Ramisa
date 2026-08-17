package com.example.data.repository

import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileRepositoryImpl : ProfileRepository {

  private val _profile = MutableStateFlow(
    AppSessionManager.currentUser.value ?: UserProfile()
  )
  override val profile: StateFlow<UserProfile> = _profile.asStateFlow()

  override suspend fun updateProfile(
    name: String,
    phone: String,
    ageCategory: AgeCategory,
    userType: UserType,
    bloodGroup: String,
    emergencyNote: String,
    guardianName: String?,
    guardianPhone: String?
  ): Result<UserProfile> {
    val updated = _profile.value.copy(
      name = name.trim(),
      phone = phone.trim(),
      ageCategory = ageCategory,
      userType = userType,
      bloodGroup = bloodGroup.trim(),
      emergencyNote = emergencyNote.trim(),
      guardianName = guardianName?.trim(),
      guardianPhone = guardianPhone?.trim(),
      updatedAt = System.currentTimeMillis()
    )
    _profile.value = updated
    AppSessionManager.setUser(updated)
    return Result.success(updated)
  }

  override suspend fun updateEmergencyPin(oldPin: String, newPin: String): Result<Boolean> {
    if (oldPin != _profile.value.emergencyPin) {
      return Result.failure(IllegalArgumentException("Current PIN is incorrect"))
    }
    if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
      return Result.failure(IllegalArgumentException("New PIN must be exactly 4 digits"))
    }
    val updated = _profile.value.copy(
      emergencyPin = newPin,
      updatedAt = System.currentTimeMillis()
    )
    _profile.value = updated
    AppSessionManager.setUser(updated)
    return Result.success(true)
  }

  override suspend fun toggleChildMode(enabled: Boolean, guardianPin: String): Result<Boolean> {
    if (guardianPin != _profile.value.emergencyPin) {
      return Result.failure(IllegalArgumentException("Invalid Guardian PIN"))
    }
    val updated = _profile.value.copy(
      isChildModeActive = enabled,
      updatedAt = System.currentTimeMillis()
    )
    _profile.value = updated
    AppSessionManager.setUser(updated)
    return Result.success(enabled)
  }
}
