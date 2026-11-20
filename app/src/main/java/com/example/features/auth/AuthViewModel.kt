package com.example.features.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.AppSessionManager
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
  val email: String = "",
  val phone: String = "",
  val name: String = "",
  val password: String = "",
  val emergencyPin: String = "",
  val confirmPin: String = "",
  val bloodGroup: String = "",
  val emergencyNote: String = "",
  val userType: UserType = UserType.ADULT,
  val ageCategory: AgeCategory = AgeCategory.ADULT_18_PLUS,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val isAuthenticated: Boolean = false,
  val restoredSession: Boolean = false,
  val awaitingEmailConfirmation: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val authRepository: AuthRepository = AuthRepositoryImpl(
    AppDatabase.getDatabase(application).userDao()
  )

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  init {
    AppSessionManager.initializeLanguage(application)
    viewModelScope.launch {
      val restored = try {
        authRepository.restoreSession()
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        false
      }
      _uiState.update { it.copy(restoredSession = restored) }
      authRepository.isAuthenticated.collect { authenticated ->
        _uiState.update { it.copy(isAuthenticated = authenticated) }
      }
    }
  }

  fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
  fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }
  fun onNameChanged(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
  fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
  fun onEmergencyPinChanged(value: String) = _uiState.update { it.copy(emergencyPin = value, errorMessage = null) }
  fun onConfirmPinChanged(value: String) = _uiState.update { it.copy(confirmPin = value, errorMessage = null) }
  fun onBloodGroupChanged(value: String) = _uiState.update { it.copy(bloodGroup = value) }
  fun onEmergencyNoteChanged(value: String) = _uiState.update { it.copy(emergencyNote = value) }
  fun onUserTypeSelected(type: UserType) = _uiState.update { it.copy(userType = type) }
  fun onAgeCategorySelected(category: AgeCategory) = _uiState.update { it.copy(ageCategory = category) }

  fun login(onSuccess: () -> Unit) {
    val state = _uiState.value
    val email = state.email.trim()
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      _uiState.update { it.copy(errorMessage = "Enter a valid email address.") }
      return
    }
    if (state.password.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Enter your account password.") }
      return
    }
    runAccountOperation(onSuccess) { authRepository.login(email, state.password) }
  }

  fun loginAsGuest(onSuccess: () -> Unit) {
    runAccountOperation(onSuccess) { authRepository.loginAsGuest() }
  }

  fun register(onSuccess: () -> Unit) {
    val state = _uiState.value
    val error = when {
      !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() -> "Enter a valid email address."
      state.password.length < 6 -> "Use at least 6 characters for your account password."
      state.name.isBlank() -> "Enter your full name."
      state.phone.count(Char::isDigit) < 7 -> "Enter a valid phone number."
      state.emergencyPin.length != 4 || state.emergencyPin.any { it !in '0'..'9' } ->
        "Enter a 4-digit emergency PIN."
      state.confirmPin != state.emergencyPin -> "The emergency PINs do not match."
      else -> null
    }
    if (error != null) {
      _uiState.update { it.copy(errorMessage = error) }
      return
    }

    runAccountOperation(onSuccess) {
      authRepository.register(
        name = state.name,
        email = state.email,
        phone = state.phone,
        password = state.password,
        emergencyPin = state.emergencyPin,
        userType = state.userType,
        ageCategory = state.ageCategory,
        bloodGroup = state.bloodGroup,
        emergencyNote = state.emergencyNote
      )
    }
  }

  private fun runAccountOperation(
    onSuccess: () -> Unit,
    operation: suspend () -> AuthResult<UserProfile>
  ) {
    if (_uiState.value.isLoading) return
    val submittedEmail = _uiState.value.email.trim()
    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    viewModelScope.launch {
      when (val result = operation()) {
        is AuthResult.Success -> {
          _uiState.value = AuthUiState(isAuthenticated = authRepository.isAuthenticated.value)
          onSuccess()
        }
        AuthResult.EmailConfirmationRequired -> {
          _uiState.value = AuthUiState(email = submittedEmail, awaitingEmailConfirmation = true)
        }
        is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
      }
    }
  }

  fun logout(onLoggedOut: () -> Unit) {
    if (_uiState.value.isLoading) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      authRepository.logout()
      _uiState.value = AuthUiState()
      onLoggedOut()
    }
  }
}
