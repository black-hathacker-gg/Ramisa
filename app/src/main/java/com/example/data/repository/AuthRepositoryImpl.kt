package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.remote.CloudAccountService
import com.example.data.remote.SupabaseAccountService
import com.example.data.remote.accountErrorMessage
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AppSessionManager {
  private val _currentUser = MutableStateFlow<UserProfile?>(null)
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  private val _isAuthenticated = MutableStateFlow(false)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  private val _isBanglaLanguage = MutableStateFlow(false)
  val isBanglaLanguage: StateFlow<Boolean> = _isBanglaLanguage.asStateFlow()
  private var languagePreferences: SharedPreferences? = null

  fun initializeLanguage(context: Context) {
    if (languagePreferences != null) return
    val preferences = context.applicationContext.getSharedPreferences("ramisa_language", Context.MODE_PRIVATE)
    languagePreferences = preferences
    _isBanglaLanguage.value = preferences.getBoolean("bangla", false)
  }

  fun toggleLanguage() {
    setBanglaLanguage(!_isBanglaLanguage.value)
  }

  fun setBanglaLanguage(isBangla: Boolean) {
    _isBanglaLanguage.value = isBangla
    languagePreferences?.edit()?.putBoolean("bangla", isBangla)?.apply()
  }

  internal fun startSession(user: UserProfile, cloudAuthenticated: Boolean) {
    _currentUser.value = user
    _isAuthenticated.value = cloudAuthenticated
  }

  internal fun setUser(user: UserProfile) {
    if (_currentUser.value?.id == user.id) _currentUser.value = user
  }

  internal fun logout() {
    _isAuthenticated.value = false
    _currentUser.value = null
  }
}

class AuthRepositoryImpl internal constructor(
  private val userDao: UserDao,
  private val cloudAccounts: CloudAccountService
) : AuthRepository {
  constructor(userDao: UserDao) : this(userDao, SupabaseAccountService)

  override val currentUser: StateFlow<UserProfile?> = AppSessionManager.currentUser
  override val isAuthenticated: StateFlow<Boolean> = AppSessionManager.isAuthenticated

  override suspend fun login(email: String, password: String): AuthResult<UserProfile> = accountOperation {
    val account = cloudAccounts.signIn(email.trim(), password)
    val profile = userDao.getProfileById(account.id)?.toDomain() ?: UserProfile(id = account.id)
    activateProfile(profile, cloudAuthenticated = true)
  }

  override suspend fun loginAsGuest(): AuthResult<UserProfile> = accountOperation {
    val guest = userDao.getGuestProfile()?.toDomain() ?: UserProfile(id = GUEST_ID, name = "Guest")
    activateProfile(guest, cloudAuthenticated = false)
  }

  override suspend fun register(
    name: String,
    email: String,
    phone: String,
    password: String,
    emergencyPin: String,
    userType: UserType,
    ageCategory: AgeCategory,
    bloodGroup: String,
    emergencyNote: String
  ): AuthResult<UserProfile> = accountOperation {
    val registration = cloudAccounts.signUp(email.trim(), password)
    val profile = UserProfile(
      id = registration.userId,
      name = name.trim(),
      phone = phone.trim(),
      emergencyPin = emergencyPin,
      userType = userType,
      ageCategory = ageCategory,
      bloodGroup = bloodGroup.trim().ifEmpty { "Unknown" },
      emergencyNote = emergencyNote.trim()
    )

    if (registration.authenticated) {
      activateProfile(profile, cloudAuthenticated = true)
    } else {
      userDao.savePendingProfile(profile.toEntity().copy(isLoggedIn = false))
      AuthResult.EmailConfirmationRequired
    }
  }

  override suspend fun logout(): Boolean = sessionMutex.withLock {
    try {
      userDao.clearProfile()
      AppSessionManager.logout()
      cloudAccounts.endSession()
      true
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      false
    }
  }

  override suspend fun restoreSession(): Boolean = sessionMutex.withLock {
    if (currentUser.value != null) return@withLock true

    val stored = userDao.getProfileSync() ?: return@withLock false
    if (stored.id.startsWith("guest_")) {
      AppSessionManager.startSession(stored.toDomain(), cloudAuthenticated = false)
      return@withLock true
    }

    val account = cloudAccounts.restoredAccount() ?: return@withLock false
    val profile = userDao.getProfileById(account.id)?.takeIf { it.isLoggedIn }
      ?: return@withLock false
    activateProfile(profile.toDomain(), cloudAuthenticated = true)
    true
  }

  private suspend fun activateProfile(profile: UserProfile, cloudAuthenticated: Boolean): AuthResult<UserProfile> {
    userDao.activateProfile(profile.toEntity())
    AppSessionManager.startSession(profile, cloudAuthenticated)
    return AuthResult.Success(profile)
  }

  private suspend fun accountOperation(block: suspend () -> AuthResult<UserProfile>): AuthResult<UserProfile> =
    sessionMutex.withLock {
      try {
        block()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        AuthResult.Error(accountErrorMessage(e))
      }
    }

  private companion object {
    const val GUEST_ID = "guest_ramisa_local"
    val sessionMutex = Mutex()
  }
}
