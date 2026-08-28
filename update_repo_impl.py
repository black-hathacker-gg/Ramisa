with open("app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt", "r") as f:
    content = f.read()

import_statement = """import com.example.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.SessionStatus
"""

content = content.replace("import com.example.domain.repository.AuthResult", import_statement + "import com.example.domain.repository.AuthResult")

# Replace login
old_login = """  override suspend fun login(phone: String, pinOrPassword: String): AuthResult<UserProfile> {
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
  }"""

new_login = """  override suspend fun login(email: String, pinOrPassword: String): AuthResult<UserProfile> {
    return try {
        SupabaseConfig.client.auth.signInWith(Email) {
            this.email = email
            this.password = pinOrPassword
        }
        
        val user = SupabaseConfig.client.auth.currentUserOrNull()
        if (user != null) {
            val profile = UserProfile(
              id = user.id,
              name = user.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: "RAMISA User",
              phone = user.userMetadata?.get("phone")?.toString()?.replace("\"", "") ?: "",
              emergencyPin = "1234",
              userType = UserType.ADULT,
              ageCategory = AgeCategory.ADULT_18_PLUS,
              bloodGroup = "B+",
              emergencyNote = ""
            )
            userDao.saveProfile(profile.toEntity().copy(isLoggedIn = true))
            AppSessionManager.setUser(profile)
            AuthResult.Success(profile)
        } else {
            AuthResult.Error("Failed to fetch user data after login")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        AuthResult.Error(e.message ?: "লগিন ব্যর্থ হয়েছে। ইমেইল এবং পাসওয়ার্ড সঠিক কিনা যাচাই করুন।")
    }
  }"""
content = content.replace(old_login, new_login)

# Replace register
old_register = """  override suspend fun register(
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
      name = name.trim().ifEmpty { "RAMISA User" },
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
  }"""

new_register = """  override suspend fun register(
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

        val newProfile = UserProfile(
          id = supabaseUserId,
          name = name.trim().ifEmpty { "RAMISA User" },
          phone = phone.trim(),
          emergencyPin = emergencyPin.trim().ifEmpty { "1234" },
          userType = userType,
          ageCategory = ageCategory,
          bloodGroup = bloodGroup.ifEmpty { "B+" },
          emergencyNote = emergencyNote
        )
        
        userDao.saveProfile(newProfile.toEntity().copy(isLoggedIn = true))
        AppSessionManager.setUser(newProfile)
        AuthResult.Success(newProfile)
    } catch (e: Exception) {
        e.printStackTrace()
        AuthResult.Error(e.message ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে। অন্য ইমেইল ব্যবহার করুন।")
    }
  }"""
content = content.replace(old_register, new_register)

with open("app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt", "w") as f:
    f.write(content)
print("AuthRepositoryImpl updated")
