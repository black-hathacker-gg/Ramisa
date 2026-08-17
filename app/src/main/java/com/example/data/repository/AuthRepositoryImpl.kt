package com.example.data.repository

import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AppSessionManager {
  private val _currentUser = MutableStateFlow<UserProfile?>(
    UserProfile(
      id = "usr_ramisa_8801",
      name = "Sadia Rahman",
      phone = "+880 1712-345678",
      ageCategory = AgeCategory.ADULT_18_PLUS,
      userType = UserType.ADULT,
      emergencyPin = "1234",
      bloodGroup = "B+",
      emergencyNote = "Asthmatic; carries emergency inhaler. In case of emergency, contact Amma or Sister immediately.",
      isChildModeActive = false,
      guardianName = "Rahman Ali (Father)",
      guardianPhone = "+880 1711-223344"
    )
  )
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  private val _isAuthenticated = MutableStateFlow(true)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  fun setUser(user: UserProfile?) {
    _currentUser.value = user
    _isAuthenticated.value = (user != null)
  }

  fun updateUser(updater: (UserProfile) -> UserProfile) {
    _currentUser.update { current ->
      current?.let(updater) ?: UserProfile().let(updater)
    }
  }

  fun logout() {
    _currentUser.value = null
    _isAuthenticated.value = false
  }
}

class AuthRepositoryImpl : AuthRepository {
  override val currentUser: StateFlow<UserProfile?> = AppSessionManager.currentUser
  override val isAuthenticated: StateFlow<Boolean> = AppSessionManager.isAuthenticated

  override suspend fun login(phone: String, pinOrPassword: String): AuthResult<UserProfile> {
    if (phone.isBlank()) {
      return AuthResult.Error("Phone number is required")
    }
    if (pinOrPassword.length < 4) {
      return AuthResult.Error("Password/PIN must be at least 4 characters")
    }

    // Authenticate with local session
    val profile = UserProfile(
      id = "usr_${System.currentTimeMillis() % 10000}",
      name = if (phone.contains("1712")) "Sadia Rahman" else "Verified Safe User",
      phone = phone.trim(),
      ageCategory = AgeCategory.ADULT_18_PLUS,
      userType = UserType.ADULT,
      emergencyPin = if (pinOrPassword.length == 4 && pinOrPassword.all { it.isDigit() }) pinOrPassword else "1234"
    )
    AppSessionManager.setUser(profile)
    return AuthResult.Success(profile)
  }

  override suspend fun register(
    name: String,
    phone: String,
    password: String,
    emergencyPin: String,
    userType: UserType,
    ageCategory: AgeCategory,
    bloodGroup: String,
    emergencyNote: String
  ): AuthResult<UserProfile> {
    if (name.isBlank()) return AuthResult.Error("Name cannot be blank")
    if (phone.isBlank()) return AuthResult.Error("Phone number cannot be blank")
    if (emergencyPin.length != 4 || !emergencyPin.all { it.isDigit() }) {
      return AuthResult.Error("Emergency PIN must be exactly 4 digits")
    }

    val newProfile = UserProfile(
      id = "usr_${System.currentTimeMillis() % 100000}",
      name = name.trim(),
      phone = phone.trim(),
      ageCategory = ageCategory,
      userType = userType,
      emergencyPin = emergencyPin,
      bloodGroup = bloodGroup.ifBlank { "Unknown" },
      emergencyNote = emergencyNote,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
    AppSessionManager.setUser(newProfile)
    return AuthResult.Success(newProfile)
  }

  override suspend fun logout(): Boolean {
    AppSessionManager.logout()
    return true
  }

  override suspend fun restoreSession(): Boolean {
    return AppSessionManager.isAuthenticated.value
  }
}
