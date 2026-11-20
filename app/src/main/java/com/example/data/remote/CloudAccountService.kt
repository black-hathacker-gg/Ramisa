package com.example.data.remote

import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal data class CloudAccount(val id: String)

internal data class CloudRegistration(val userId: String, val authenticated: Boolean)

internal interface CloudAccountService {
    suspend fun signIn(email: String, password: String): CloudAccount
    suspend fun signUp(email: String, password: String): CloudRegistration
    suspend fun restoredAccount(): CloudAccount?
    suspend fun endSession()
}

internal object SupabaseAccountService : CloudAccountService {
    override suspend fun signIn(email: String, password: String): CloudAccount {
        val auth = SupabaseConfig.client.auth
        auth.awaitInitialization()
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return CloudAccount(checkNotNull(auth.currentUserOrNull()?.id))
    }

    override suspend fun signUp(email: String, password: String): CloudRegistration {
        val auth = SupabaseConfig.client.auth
        auth.awaitInitialization()
        val pendingUser = auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        if (pendingUser != null) return CloudRegistration(pendingUser.id, authenticated = false)
        return CloudRegistration(checkNotNull(auth.currentUserOrNull()?.id), authenticated = true)
    }

    override suspend fun restoredAccount(): CloudAccount? {
        val auth = SupabaseConfig.clientOrNull?.auth ?: return null
        return withTimeoutOrNull(4_000) {
            auth.awaitInitialization()
            auth.currentUserOrNull()?.id?.let(::CloudAccount)
        }
    }

    override suspend fun endSession() {
        val auth = SupabaseConfig.clientOrNull?.auth ?: return
        try {
            withTimeoutOrNull(4_000) {
                auth.awaitInitialization()
                auth.signOut(SignOutScope.LOCAL)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // The local session can still be removed when the device has no connection.
        } finally {
            withContext(NonCancellable) { auth.clearSession() }
        }
    }
}

internal fun accountErrorMessage(error: Throwable): String {
    if (error is CloudAccountUnavailable) return error.message.orEmpty()
    if (error is AuthRestException) {
        when (error.errorCode) {
            AuthErrorCode.InvalidCredentials, AuthErrorCode.UserNotFound ->
                return "The email or password is incorrect."
            AuthErrorCode.EmailNotConfirmed ->
                return "Confirm your email using the link in your inbox, then sign in."
            AuthErrorCode.EmailAddressNotAuthorized ->
                return "The account service can’t send a confirmation to this address. You can continue as a guest."
            AuthErrorCode.OverEmailSendRateLimit ->
                return "The account service has reached its email limit. Try again later."
            AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists ->
                return "An account already exists for this email. Try signing in."
            AuthErrorCode.WeakPassword ->
                return "Choose a stronger password and try again."
            AuthErrorCode.SignupDisabled, AuthErrorCode.EmailProviderDisabled ->
                return "Email accounts are currently disabled on the server."
            else -> Unit
        }
    }
    if (error is RestException) {
        return when (error.statusCode) {
            401, 403, 404 -> "The account service rejected this app’s configuration. You can continue as a guest."
            429 -> "Too many requests. Wait a moment and try again."
            in 500..599 -> "The account service is temporarily unavailable. Try again later or continue as a guest."
            else -> "The account request was rejected. Check your details and try again."
        }
    }
    val causes = generateSequence(error) { it.cause?.takeUnless { cause -> cause === it } }.take(10).toList()
    return when {
        causes.any { it is SSLException } ->
            "A secure connection to the account service could not be made. Check the device date and connection."
        causes.any { it is HttpRequestTimeoutException } ->
            "The account service took too long to respond. Try again."
        causes.any { it is UnknownHostException || it is IOException } ->
            "The account service could not be reached. Check your connection or continue as a guest."
        else -> "The account request could not be completed. Please try again."
    }
}
