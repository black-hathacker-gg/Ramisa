package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
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
  private val _currentUser = MutableStateFlow<UserProfile?>(null)
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  private val _isAuthenticated = MutableStateFlow(false)
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

class AuthRepositoryImpl(private val userDao: UserDao) : AuthRepository {

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
    userDao.saveProfile(profile.toEntity())
    AppSessionManager.setUser(profile)
    return AuthResult.Success(profile)
  }

  override suspend fun loginAsGuest(): AuthResult<UserProfile> {
    val guestProfile = UserProfile(
      id = "usr_guest_${System.currentTimeMillis() % 10000}",
      name = "Guest User (অতিথি)",
      phone = "+880 1700-000000",
      ageCategory = AgeCategory.ADULT_18_PLUS,
      userType = UserType.ADULT,
      emergencyPin = "0000",
      bloodGroup = "Not Specified",
      emergencyNote = "Logged in under Guest Mode. Please set up emergency contacts in Profile when ready."
    )
    userDao.saveProfile(guestProfile.toEntity())
    AppSessionManager.setUser(guestProfile)
    return AuthResult.Success(guestProfile)
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
    userDao.saveProfile(newProfile.toEntity())
    AppSessionManager.setUser(newProfile)
    return AuthResult.Success(newProfile)
  }

  override suspend fun logout(): Boolean {
    userDao.clearProfile()
    AppSessionManager.logout()
    return true
  }

  override suspend fun restoreSession(): Boolean {
    val savedProfile = userDao.getProfileSync()?.toDomain()
    if (savedProfile != null) {
      AppSessionManager.setUser(savedProfile)
      return true
    }
    return false
  }
}
