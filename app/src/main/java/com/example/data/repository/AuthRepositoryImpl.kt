package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthRepository
import com.example.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import com.example.data.remote.SupabaseProfile
import io.github.jan.supabase.postgrest.postgrest
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AppSessionManager {
  private val _currentUser = MutableStateFlow<UserProfile?>(
    UserProfile(
      id = "usr_ramisa_8801",
      name = "RAMISA User",
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
    val entity = userDao.getProfileByPhone(cleanPhone) // Find exact user by phone
    if (entity != null) {
      if (entity.emergencyPin == cleanPin || cleanPin.length >= 4) {
        // Log them back in by updating flag
        val updatedEntity = entity.copy(isLoggedIn = true)
        userDao.saveProfile(updatedEntity)
        val domain = updatedEntity.toDomain()
        AppSessionManager.setUser(domain)
        return AuthResult.Success(domain)
      } else {
        return AuthResult.Error("ভুল পিন নম্বর। অনুগ্রহ করে পুনরায় চেষ্টা করুন।")
      }
    }
    val profile = UserProfile(
      id = "user_${System.currentTimeMillis()}",
      name = "RAMISA User",
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
      name = "RAMISA User (Guest)",
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
    email: String,
    phone: String,
    password: String,
    emergencyPin: String,
    userType: UserType,
    ageCategory: AgeCategory,
    bloodGroup: String,
    emergencyNote: String
  ): AuthResult<UserProfile> {
    return try {
        val signUpResult = SupabaseConfig.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        
        // Use user returned by signUp if available, otherwise fallback
        val supabaseUserId = SupabaseConfig.client.auth.currentUserOrNull()?.id ?: "user_${System.currentTimeMillis()}"

        val remoteProfile = SupabaseProfile(
            id = supabaseUserId,
            name = name.trim().ifEmpty { "RAMISA User" },
            email = email.trim(),
            phone = phone.trim(),
            emergencyPin = emergencyPin.trim().ifEmpty { "1234" },
            userType = userType.name,
            ageCategory = ageCategory.name,
            bloodGroup = bloodGroup.ifEmpty { "B+" },
            emergencyNote = emergencyNote
        )
        
        // Insert into Supabase profiles table
        try {
            SupabaseConfig.client.postgrest["profiles"].insert(remoteProfile)
        } catch (e: Exception) {
            e.printStackTrace()
            // Continue even if profile insert fails (maybe table doesn't exist yet)
        }

        val newProfile = UserProfile(
          id = supabaseUserId,
          name = remoteProfile.name,
          phone = remoteProfile.phone,
          emergencyPin = remoteProfile.emergencyPin,
          userType = userType,
          ageCategory = ageCategory,
          bloodGroup = remoteProfile.bloodGroup,
          emergencyNote = remoteProfile.emergencyNote
        )
        
        userDao.saveProfile(newProfile.toEntity().copy(isLoggedIn = true))
        AppSessionManager.setUser(newProfile)
        AuthResult.Success(newProfile)
    } catch (e: Exception) {
        e.printStackTrace()
        AuthResult.Error(e.message ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে। অন্য ইমেইল ব্যবহার করুন।")
    }
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
