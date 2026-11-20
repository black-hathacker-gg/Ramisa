package com.example.features.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.GhostModeManager
import com.example.data.local.AppDatabase
import com.example.data.repository.ProfileRepositoryImpl
import com.example.features.lockscreen.LockScreenSosManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
  val isGhostModeEnabled: Boolean = false,
  val isLockScreenGuardEnabled: Boolean = false,
  val isDirectSmsEnabled: Boolean = false,
  val isSavingPin: Boolean = false,
  val pinError: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
  private val profileRepository = ProfileRepositoryImpl(AppDatabase.getDatabase(application).userDao())
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState = _uiState.asStateFlow()

  init { refreshDeviceSettings() }

  fun refreshDeviceSettings() {
    val app = getApplication<Application>()
    _uiState.update {
      it.copy(
        isGhostModeEnabled = GhostModeManager.isGhostModeEnabled(app),
        isLockScreenGuardEnabled = LockScreenSosManager.isGuardEnabled(app),
        isDirectSmsEnabled = LockScreenSosManager.isDirectSmsEnabled(app)
      )
    }
  }

  fun setGhostMode(enabled: Boolean) {
    GhostModeManager.setGhostMode(getApplication(), enabled)
    refreshDeviceSettings()
  }

  fun setLockScreenGuard(enabled: Boolean) {
    LockScreenSosManager.setLockScreenGuardEnabled(getApplication(), enabled)
    refreshDeviceSettings()
  }

  fun setDirectSms(enabled: Boolean) {
    LockScreenSosManager.setDirectSmsEnabled(getApplication(), enabled)
    refreshDeviceSettings()
  }

  fun clearPinError() {
    _uiState.update { it.copy(pinError = null) }
  }

  fun updatePin(currentPin: String, newPin: String, onSaved: () -> Unit) {
    if (_uiState.value.isSavingPin) return
    _uiState.update { it.copy(isSavingPin = true, pinError = null) }
    viewModelScope.launch {
      val result = profileRepository.updateEmergencyPin(currentPin, newPin)
      _uiState.update {
        it.copy(
          isSavingPin = false,
          pinError = if (result.isSuccess) null else result.exceptionOrNull()?.message ?: "Could not save the PIN"
        )
      }
      if (result.isSuccess) onSaved()
    }
  }
}
