package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.toEntity
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileRepositoryImpl(private val userDao: UserDao) : ProfileRepository {

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
    val current = AppSessionManager.currentUser.value ?: return Result.failure(noProfile())
    val updated = current.copy(
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
    return saveChanges(updated)
  }

  override suspend fun updateEmergencyPin(oldPin: String, newPin: String): Result<Boolean> {
    val current = AppSessionManager.currentUser.value ?: return Result.failure(noProfile())
    if (oldPin != current.emergencyPin) {
      return Result.failure(IllegalArgumentException("Current PIN is incorrect"))
    }
    if (newPin.length != 4 || newPin.any { it !in '0'..'9' }) {
      return Result.failure(IllegalArgumentException("New PIN must be exactly 4 digits"))
    }

    val updated = current.copy(
      emergencyPin = newPin,
      updatedAt = System.currentTimeMillis()
    )
    return saveChanges(updated).map { true }
  }

  override suspend fun toggleChildMode(enabled: Boolean, guardianPin: String): Result<Boolean> {
    val current = AppSessionManager.currentUser.value ?: return Result.failure(noProfile())
    if (current.emergencyPin.isBlank() || guardianPin != current.emergencyPin) {
      return Result.failure(IllegalArgumentException("Invalid Guardian PIN"))
    }
    val updated = current.copy(
      isChildModeActive = enabled,
      updatedAt = System.currentTimeMillis()
    )
    return saveChanges(updated).map { enabled }
  }

  private suspend fun saveChanges(profile: UserProfile): Result<UserProfile> {
    return try {
      if (!userDao.updateActiveProfile(profile.toEntity())) return Result.failure(noProfile())
      _profile.value = profile
      AppSessionManager.setUser(profile)
      Result.success(profile)
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      Result.failure(IllegalStateException("The profile could not be saved on this device."))
    }
  }

  private fun noProfile() = IllegalStateException("Open RAMISA as a guest or sign in to edit your profile.")
}
