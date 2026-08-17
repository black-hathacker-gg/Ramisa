package com.example.features.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppLanguage {
  ENGLISH,
  BANGLA
}

data class SettingsUiState(
  val language: AppLanguage = AppLanguage.ENGLISH,
  val isSmsFallbackEnabled: Boolean = true,
  val isChildSafetyModeEnabled: Boolean = false,
  val isAutoRecordEnabled: Boolean = false,
  val emergencyPin: String = "1234",
  val nationalHotline: String = "999",
  val womenChildHelpline: String = "109",
  val nationalEmergencyContactName: String = "National Emergency (জাতীয় জরুরি সেবা)"
)

class SettingsViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  fun setLanguage(lang: AppLanguage) = _uiState.update { it.copy(language = lang) }
  fun toggleSmsFallback() = _uiState.update { it.copy(isSmsFallbackEnabled = !it.isSmsFallbackEnabled) }
  fun toggleChildMode() = _uiState.update { it.copy(isChildSafetyModeEnabled = !it.isChildSafetyModeEnabled) }
  fun toggleAutoRecord() = _uiState.update { it.copy(isAutoRecordEnabled = !it.isAutoRecordEnabled) }
  fun updatePin(newPin: String) = _uiState.update { it.copy(emergencyPin = newPin) }
}
