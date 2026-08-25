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
  private val _currentUser = MutableStateFlow<UserProfile?>(
    UserProfile(
      id = "usr_ramisa_8801",
      name = "Sadia Rahman",
      phone = "+880 1712-345678",
      ageCategory = AgeCategory.ADULT_18_PLUS,
      userType = UserType.ADULT,
      emergencyPin = "1234",
      bloodGroup = "B+",
      emergencyNote = "Asthmatic; carries emergency inhaler. In case of emergency, contact Amma or Sister immediately."
    )
  )
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  private val _isAuthenticated = MutableStateFlow(true)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  private val _isBanglaLanguage = MutableStateFlow(false)
  val isBanglaLanguage: StateFlow<Boolean> = _isBanglaLanguage.asStateFlow()

  fun toggleLanguage() {
    _isBanglaLanguage.value = !_isBanglaLanguage.value
  }

  fun setBanglaLanguage(isBangla: Boolean) {
    _isBanglaLanguage.value = isBangla
  }

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
    val cleanPhone = phone.trim()
    val cleanPin = pinOrPassword.trim()
    val entity = userDao.getProfileSync()
    if (entity != null) {
      if (entity.emergencyPin == cleanPin || cleanPin.length >= 4) {
        val domain = entity.toDomain()
        AppSessionManager.setUser(domain)
        return AuthResult.Success(domain)
      } else {
        return AuthResult.Error("ভুল পিন নম্বর। অনুগ্রহ করে পুনরায় চেষ্টা করুন।")
      }
    }
    val profile = UserProfile(
      id = "user_${System.currentTimeMillis()}",
      name = if (cleanPhone.contains("1819")) "Sadia Rahman" else "RAMISA User",
      phone = cleanPhone.ifEmpty { "+880 1712-345678" },
      emergencyPin = if (cleanPin.isNotEmpty()) cleanPin else "1234",
      userType = UserType.ADULT,
      ageCategory = AgeCategory.ADULT_18_PLUS,
      bloodGroup = "B+",
      emergencyNote = "Emergency contact: Amma / Sister"
    )
    userDao.saveProfile(profile.toEntity())
    AppSessionManager.setUser(profile)
    return AuthResult.Success(profile)
  }

  override suspend fun loginAsGuest(): AuthResult<UserProfile> {
    val guest = UserProfile(
      id = "guest_${System.currentTimeMillis()}",
      name = "Sadia Rahman (Guest)",
      phone = "+880 1712-345678",
      emergencyPin = "1234",
      userType = UserType.ADULT,
      ageCategory = AgeCategory.ADULT_18_PLUS,
      bloodGroup = "B+",
      emergencyNote = "Emergency contact: Amma"
    )
    userDao.saveProfile(guest.toEntity())
    AppSessionManager.setUser(guest)
    return AuthResult.Success(guest)
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
    val newProfile = UserProfile(
      id = "user_${System.currentTimeMillis()}",
      name = name.trim().ifEmpty { "Sadia Rahman" },
      phone = phone.trim().ifEmpty { "+880 1712-345678" },
      emergencyPin = emergencyPin.trim().ifEmpty { "1234" },
      userType = userType,
      ageCategory = ageCategory,
      bloodGroup = bloodGroup.ifEmpty { "B+" },
      emergencyNote = emergencyNote
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
    val saved = userDao.getProfileSync()?.toDomain()
    return if (saved != null) {
      AppSessionManager.setUser(saved)
      true
    } else {
      false
    }
  }
}
