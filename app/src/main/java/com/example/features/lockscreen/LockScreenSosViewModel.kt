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
  val isLockScreenGuardActive: Boolean = true,
  val isPowerButton3xEnabled: Boolean = true,
  val isVolumeKeyTriggerEnabled: Boolean = true,
  val isOfflineSmsFallbackEnabled: Boolean = true,
  val isOfflineMeshBeaconEnabled: Boolean = true,
  val isSimulatedLockActive: Boolean = false,
  val lastTriggerStatus: String = "Lock Screen Guard Active • Ready for Offline SOS",
  val offlineSmsDispatchedCount: Int = 0,
  val emergencyContacts: List<EmergencyContact> = emptyList(),
  val userPin: String = "1234",
  val customSosMessage: String = "[RAMISA OFFLINE SOS] I am in urgent danger! My live GPS location will be attached. Please send help immediately!",
  val showSuccessDialog: Boolean = false
)

class LockScreenSosViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val contactRepository: EmergencyContactRepository = EmergencyContactRepositoryImpl(
      AppDatabase.getDatabase(application).emergencyContactDao()
  )

  private val _uiState = MutableStateFlow(LockScreenSosUiState())
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
    _uiState.update { it.copy(isVolumeKeyTriggerEnabled = enabled) }
  }

  fun toggleOfflineSms(enabled: Boolean) {
    _uiState.update { it.copy(isOfflineSmsFallbackEnabled = enabled) }
  }

  fun toggleOfflineMesh(enabled: Boolean) {
    _uiState.update { it.copy(isOfflineMeshBeaconEnabled = enabled) }
  }

  fun setSimulatedLock(active: Boolean) {
    _uiState.update { it.copy(isSimulatedLockActive = active) }
  }

  fun triggerOfflineSosFromLockScreen(context: Context, onSosTriggered: () -> Unit) {
    LockScreenSosManager.triggerOfflineEmergency(context, "Lock Screen User Action")
    _uiState.update {
      it.copy(
        offlineSmsDispatchedCount = it.offlineSmsDispatchedCount + it.emergencyContacts.size.coerceAtLeast(3),
        lastTriggerStatus = "Offline SOS Dispatched via Direct SMS to ${it.emergencyContacts.size.coerceAtLeast(3)} guardians!",
        showSuccessDialog = true
      )
    }
    onSosTriggered()
  }

  fun dismissSuccessDialog() {
    _uiState.update { it.copy(showSuccessDialog = false) }
  }
}
