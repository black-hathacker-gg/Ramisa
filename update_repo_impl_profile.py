with open("app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt", "r") as f:
    content = f.read()

import_statement = """import com.example.data.remote.SupabaseProfile
import io.github.jan.supabase.postgrest.postgrest
"""

content = content.replace("import com.example.domain.repository.AuthResult", import_statement + "import com.example.domain.repository.AuthResult")

# Replace login
old_login = """  override suspend fun login(email: String, pinOrPassword: String): AuthResult<UserProfile> {
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

new_login = """  override suspend fun login(email: String, pinOrPassword: String): AuthResult<UserProfile> {
    return try {
        SupabaseConfig.client.auth.signInWith(Email) {
            this.email = email
            this.password = pinOrPassword
        }
        
        val user = SupabaseConfig.client.auth.currentUserOrNull()
        if (user != null) {
            // Fetch profile from Supabase profiles table
            val remoteProfile = try {
                SupabaseConfig.client.postgrest["profiles"].select {
                    filter { eq("id", user.id) }
                }.decodeSingleOrNull<SupabaseProfile>()
            } catch (e: Exception) {
                null
            }

            val profile = if (remoteProfile != null) {
                UserProfile(
                    id = remoteProfile.id,
                    name = remoteProfile.name,
                    phone = remoteProfile.phone,
                    emergencyPin = remoteProfile.emergencyPin,
                    userType = try { UserType.valueOf(remoteProfile.userType) } catch(e: Exception) { UserType.ADULT },
                    ageCategory = try { AgeCategory.valueOf(remoteProfile.ageCategory) } catch(e: Exception) { AgeCategory.ADULT_18_PLUS },
                    bloodGroup = remoteProfile.bloodGroup,
                    emergencyNote = remoteProfile.emergencyNote
                )
            } else {
                UserProfile(
                  id = user.id,
                  name = "RAMISA User",
                  phone = "",
                  emergencyPin = "1234",
                  userType = UserType.ADULT,
                  ageCategory = AgeCategory.ADULT_18_PLUS,
                  bloodGroup = "B+",
                  emergencyNote = ""
                )
            }
            
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
  }"""
content = content.replace(old_register, new_register)

with open("app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt", "w") as f:
    f.write(content)
print("AuthRepositoryImpl updated with Profile")
