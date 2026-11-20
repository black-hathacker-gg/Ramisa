package com.example.features.lockscreen

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.AppSessionManager
import com.example.data.repository.EmergencyContactRepositoryImpl
import com.example.domain.model.EmergencyContact
import com.example.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockScreenSosUiState(
  val isLockScreenGuardActive: Boolean = false,
  val isPowerButton3xEnabled: Boolean = false,
  val isVolumeKeyTriggerEnabled: Boolean = false,
  val isOfflineSmsFallbackEnabled: Boolean = false,
  val isOfflineMeshBeaconEnabled: Boolean = false,
  val isSimulatedLockActive: Boolean = false,
  val emergencyContacts: List<EmergencyContact> = emptyList()
)

class LockScreenSosViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val contactRepository: EmergencyContactRepository = EmergencyContactRepositoryImpl(
      AppDatabase.getDatabase(application).emergencyContactDao()
  )

  private val _uiState = MutableStateFlow(LockScreenSosUiState(
    isLockScreenGuardActive = LockScreenSosManager.isGuardEnabled(application),
    isVolumeKeyTriggerEnabled = LockScreenSosManager.isVolumeShortcutEnabled(application),
    isOfflineSmsFallbackEnabled = LockScreenSosManager.isDirectSmsEnabled(application)
  ))
  val uiState: StateFlow<LockScreenSosUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      contactRepository.contacts.collect { list ->
        _uiState.update { it.copy(emergencyContacts = list) }
      }
    }
  }

  fun toggleLockScreenGuard(context: Context, enabled: Boolean) {
    LockScreenSosManager.setLockScreenGuardEnabled(context, enabled)
    _uiState.update { it.copy(isLockScreenGuardActive = enabled) }
  }

  fun togglePowerButton3x(enabled: Boolean) {
    _uiState.update { it.copy(isPowerButton3xEnabled = enabled) }
  }

  fun toggleVolumeKeyTrigger(enabled: Boolean) {
    LockScreenSosManager.setVolumeShortcutEnabled(getApplication(), enabled)
    _uiState.update { it.copy(isVolumeKeyTriggerEnabled = enabled) }
  }

  fun toggleOfflineSms(enabled: Boolean) {
    LockScreenSosManager.setDirectSmsEnabled(getApplication(), enabled)
    _uiState.update { it.copy(isOfflineSmsFallbackEnabled = enabled) }
  }

  fun toggleOfflineMesh(enabled: Boolean) {
    _uiState.update { it.copy(isOfflineMeshBeaconEnabled = enabled) }
  }

  fun setSimulatedLock(active: Boolean) {
    _uiState.update { it.copy(isSimulatedLockActive = active) }
  }

}
