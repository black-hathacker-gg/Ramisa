package com.example

import com.example.data.local.UserDao
import com.example.data.local.UserProfileEntity
import com.example.data.local.toEntity
import com.example.data.remote.ClientEndpoint
import com.example.data.remote.CloudAccount
import com.example.data.remote.CloudAccountService
import com.example.data.remote.CloudAccountUnavailable
import com.example.data.remote.CloudRegistration
import com.example.data.remote.accountErrorMessage
import com.example.data.repository.AppSessionManager
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.ProfileRepositoryImpl
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthResult
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAndProfileUnitTest {
  private val userDao = MemoryUserDao()
  private val cloud = FakeCloudAccounts()
  private val auth = AuthRepositoryImpl(userDao, cloud)

  @After fun clearSession() { AppSessionManager.logout() }

  @Test fun `HTTPS origin and public key are required`() {
    val publicKey = "sb_publishable_abcdefghijklmnop"
    assertEquals(
      "https://example.supabase.co",
      ClientEndpoint.parse("https://example.supabase.co/", publicKey)?.url
    )
    assertNull(ClientEndpoint.parse("http://example.supabase.co", publicKey))
    assertNull(ClientEndpoint.parse("https://your-project.supabase.co", publicKey))
    assertNull(ClientEndpoint.parse("https://example.supabase.co/auth/v1", publicKey))
    assertNull(ClientEndpoint.parse("https://example.supabase.co", "sb_secret_abcdefghijklmnop"))
    assertNull(ClientEndpoint.parse("https://example.supabase.co", "YOUR_SUPABASE_PUBLIC_KEY"))
    val legacyAnonKey = "test.eyJyb2xlIjoiYW5vbiJ9.signature"
    val serviceRoleKey = "test.eyJyb2xlIjoic2VydmljZV9yb2xlIn0.signature"
    assertTrue(ClientEndpoint.parse("https://example.supabase.co", legacyAnonKey) != null)
    assertNull(ClientEndpoint.parse("https://example.supabase.co", serviceRoleKey))
  }

  @Test fun `TLS errors use a safe message without URLs or secrets`() {
    val error = IllegalStateException("private information", SSLHandshakeException("untrusted certificate"))
    val message = accountErrorMessage(error)
    assertTrue(message.contains("secure connection"))
    assertFalse(message.contains("private information"))
    assertFalse(message.contains("untrusted certificate"))
  }

  @Test fun `an offline account service leaves sign in unauthenticated`() = runTest {
    cloud.signInFailure = CloudAccountUnavailable()
    val result = auth.login("demo@example.com", "any-password")
    assertTrue(result is AuthResult.Error && result.message.contains("continue as a guest"))
    assertFalse(auth.isAuthenticated.value)
    assertNull(userDao.getProfileSync())
  }

  @Test fun `a registered profile stays inactive until sign in succeeds`() = runTest {
    assertEquals(AuthResult.EmailConfirmationRequired, register())
    assertFalse(auth.isAuthenticated.value)
    assertNull(userDao.getProfileSync())
    assertEquals("9876", userDao.getProfileById("cloud-id")?.emergencyPin)

    cloud.signInFailure = IllegalArgumentException("Test server rejected invalid credentials")
    assertTrue(auth.login("demo@example.com", "a-wrong-password") is AuthResult.Error)
    assertFalse(auth.isAuthenticated.value)

    cloud.signInFailure = null
    val result = auth.login(" demo@example.com ", " demo-password ")
    assertTrue(result is AuthResult.Success)
    assertEquals("demo@example.com", cloud.lastEmail)
    assertEquals(" demo-password ", cloud.lastPassword)
    assertTrue(auth.isAuthenticated.value)
    assertEquals("Nusrat Jahan", userDao.getProfileSync()?.name)
    assertEquals("9876", auth.currentUser.value?.emergencyPin)
  }

  @Test fun `a legacy local profile cannot masquerade as a cloud session`() = runTest {
    userDao.saveProfile(UserProfile(id = "user_legacy").toEntity())
    cloud.restoredId = "unrelated-server-id"
    assertFalse(auth.restoreSession())
    assertFalse(auth.isAuthenticated.value)
    assertNull(auth.currentUser.value)
  }

  @Test fun `an issued session can be restored and then signed out`() = runTest {
    cloud.confirmationRequired = false
    assertTrue(register() is AuthResult.Success)
    assertTrue(auth.isAuthenticated.value)
    AppSessionManager.logout()
    assertTrue(auth.restoreSession())
    assertTrue(auth.isAuthenticated.value)
    assertTrue(auth.logout())
    assertFalse(auth.restoreSession())
    assertFalse(auth.isAuthenticated.value)
    assertEquals("Nusrat Jahan", userDao.getProfileById("cloud-id")?.name)
  }

  @Test fun `guest profile is editable, restorable, and separate from cloud sign in`() = runTest {
    cloud.signInFailure = CloudAccountUnavailable()
    assertTrue(auth.loginAsGuest() is AuthResult.Success)
    assertFalse(auth.isAuthenticated.value)
    val profile = ProfileRepositoryImpl(userDao)
    assertTrue(profile.updateProfile(
      name = "Tahmina Akter",
      phone = "+880 1912-998877",
      ageCategory = AgeCategory.ADULT_18_PLUS,
      userType = UserType.GUARDIAN,
      bloodGroup = "AB+",
      emergencyNote = "Test note",
      guardianName = "Akter Hossain",
      guardianPhone = null
    ).isSuccess)
    assertTrue(profile.toggleChildMode(true, "").isFailure)
    assertTrue(profile.updateEmergencyPin("", "0987").isSuccess)

    // Simulate reopening the app without its process-local session.
    AppSessionManager.logout()
    assertTrue(auth.restoreSession())
    assertFalse(auth.isAuthenticated.value)
    assertEquals("AB+", auth.currentUser.value?.bloodGroup)
    assertEquals("Akter Hossain", auth.currentUser.value?.guardianName)
    assertTrue(auth.logout())
    assertFalse(auth.restoreSession())
    assertNull(auth.currentUser.value)
    assertTrue(auth.loginAsGuest() is AuthResult.Success)
    assertEquals("Tahmina Akter", auth.currentUser.value?.name)
  }

  private suspend fun register() = auth.register(
    name = "Nusrat Jahan",
    email = "demo@example.com",
    phone = "+880 1812-345678",
    password = "demo-password",
    emergencyPin = "9876",
    userType = UserType.ADULT,
    ageCategory = AgeCategory.ADULT_18_PLUS,
    bloodGroup = "AB+",
    emergencyNote = "Test note"
  )
}

private class FakeCloudAccounts : CloudAccountService {
  var signInFailure: Exception? = null
  var confirmationRequired = true
  var restoredId: String? = null
  var lastEmail: String? = null
  var lastPassword: String? = null

  override suspend fun signIn(email: String, password: String): CloudAccount {
    signInFailure?.let { throw it }
    lastEmail = email
    lastPassword = password
    restoredId = "cloud-id"
    return CloudAccount("cloud-id")
  }

  override suspend fun signUp(email: String, password: String): CloudRegistration {
    if (!confirmationRequired) restoredId = "cloud-id"
    return CloudRegistration("cloud-id", authenticated = !confirmationRequired)
  }
  override suspend fun restoredAccount(): CloudAccount? = restoredId?.let(::CloudAccount)
  override suspend fun endSession() { restoredId = null }
}

private class MemoryUserDao : UserDao {
  private val profiles = mutableMapOf<String, UserProfileEntity>()
  private val active = MutableStateFlow<UserProfileEntity?>(null)

  override fun getProfile(): Flow<UserProfileEntity?> = active
  override suspend fun getProfileById(id: String) = profiles[id]
  override suspend fun getGuestProfile() = profiles.values.lastOrNull { it.id.startsWith("guest_") }
  override suspend fun getProfileSync() = active.value

  override suspend fun saveProfile(profile: UserProfileEntity) {
    profiles[profile.id] = profile
    if (profile.isLoggedIn) active.value = profile
  }

  override suspend fun savePendingProfile(profile: UserProfileEntity) {
    profiles.putIfAbsent(profile.id, profile)
  }

  override suspend fun clearProfile() {
    profiles.replaceAll { _, profile -> profile.copy(isLoggedIn = false) }
    active.value = null
  }
}
