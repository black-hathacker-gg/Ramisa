package com.example.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.remote.SupabaseAccountService
import com.example.data.remote.SupabaseConfig
import com.example.data.remote.accountErrorMessage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupabaseAuthSmokeTest {
  @Test
  fun rejectsInvalidCredentialsAndInvalidSignupOverHttps() {
    assumeTrue(InstrumentationRegistry.getArguments().getString("liveAuthSmoke") == "true")
    runBlocking {
      withTimeout(45_000) {
        val auth = SupabaseConfig.client.auth
        auth.awaitInitialization()
        val priorUser = auth.currentUserOrNull()?.id

        val signInError = runCatching {
          SupabaseAccountService.signIn("${UUID.randomUUID()}@example.invalid", UUID.randomUUID().toString())
        }.exceptionOrNull()
        assertTrue(
          "The Auth server must reject the synthetic login as invalid credentials.",
          signInError is AuthRestException && signInError.errorCode == AuthErrorCode.InvalidCredentials
        )
        assertEquals("The email or password is incorrect.", accountErrorMessage(checkNotNull(signInError)))

        val signUpError = runCatching {
          SupabaseAccountService.signUp("invalid-email-address", "")
        }.exceptionOrNull()
        assertTrue(
          "The Auth server must reject signup with a malformed email and an empty password.",
          signUpError is AuthRestException && signUpError.statusCode in setOf(400, 422)
        )
        assertEquals(priorUser, auth.currentUserOrNull()?.id)
      }
    }
  }
}
