package com.example.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepositoryImpl
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
  val phone: String = "+880 1712-345678",
  val name: String = "",
  val password: String = "",
  val emergencyPin: String = "",
  val confirmPin: String = "",
  val bloodGroup: String = "B+",
  val emergencyNote: String = "",
  val userType: UserType = UserType.ADULT,
  val ageCategory: AgeCategory = AgeCategory.ADULT_18_PLUS,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val isAuthenticated: Boolean = true
)

class AuthViewModel(
  private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      authRepository.isAuthenticated.collect { authed ->
        _uiState.update { it.copy(isAuthenticated = authed) }
      }
    }
  }

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
    if (state.phone.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter your phone number") }
      return
    }
    if (state.password.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter your password or PIN") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }
      when (val result = authRepository.login(state.phone, state.password)) {
        is AuthResult.Success -> {
          _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
          onSuccess()
        }
        is AuthResult.Error -> {
          _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
      }
    }
  }

  fun register(onSuccess: () -> Unit) {
    val state = _uiState.value
    if (state.name.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter your full name") }
      return
    }
    if (state.phone.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter a valid phone number") }
      return
    }
    if (state.emergencyPin.length != 4 || !state.emergencyPin.all { it.isDigit() }) {
      _uiState.update { it.copy(errorMessage = "Emergency PIN must be exactly 4 digits") }
      return
    }
    if (state.confirmPin.isNotEmpty() && state.confirmPin != state.emergencyPin) {
      _uiState.update { it.copy(errorMessage = "Emergency PIN confirmation does not match") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }
      val result = authRepository.register(
        name = state.name,
        phone = state.phone,
        password = state.password.ifBlank { "Pass@1234" },
        emergencyPin = state.emergencyPin,
        userType = state.userType,
        ageCategory = state.ageCategory,
        bloodGroup = state.bloodGroup,
        emergencyNote = state.emergencyNote
      )
      when (result) {
        is AuthResult.Success -> {
          _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
          onSuccess()
        }
        is AuthResult.Error -> {
          _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
      }
    }
  }

  fun logout(onLoggedOut: () -> Unit) {
    viewModelScope.launch {
      authRepository.logout()
      _uiState.update { it.copy(isAuthenticated = false) }
      onLoggedOut()
    }
  }
}
