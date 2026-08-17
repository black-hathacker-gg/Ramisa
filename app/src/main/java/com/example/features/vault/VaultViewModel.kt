package com.example.features.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppSessionManager
import com.example.data.repository.VaultRepositoryImpl
import com.example.domain.model.VaultEvidence
import com.example.domain.repository.VaultRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultUiState(
  val isUnlocked: Boolean = false,
  val enteredPin: String = "",
  val pinErrorMessage: String? = null,
  val evidenceList: List<VaultEvidence> = emptyList(),
  val isAddNoteDialogVisible: Boolean = false,
  val isRecordingAudio: Boolean = false,
  val recordingSeconds: Int = 0,
  val feedbackMessage: String? = null
)

class VaultViewModel(
  private val vaultRepository: VaultRepository = VaultRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(VaultUiState())
  val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

  private var recordingJob: Job? = null

  init {
    viewModelScope.launch {
      vaultRepository.isVaultUnlocked.collect { unlocked ->
        _uiState.update { it.copy(isUnlocked = unlocked) }
      }
    }
    viewModelScope.launch {
      vaultRepository.evidenceList.collect { list ->
        _uiState.update { it.copy(evidenceList = list) }
      }
    }
  }

  fun onPinChange(pin: String) {
    _uiState.update { it.copy(enteredPin = pin, pinErrorMessage = null) }
  }

  fun unlockVault() {
    val expectedPin = AppSessionManager.currentUser.value?.emergencyPin ?: "1234"
    viewModelScope.launch {
      val res = vaultRepository.unlockVault(_uiState.value.enteredPin, expectedPin)
      if (res.isSuccess) {
        _uiState.update { it.copy(enteredPin = "", pinErrorMessage = null) }
      } else {
        _uiState.update {
          it.copy(pinErrorMessage = res.exceptionOrNull()?.message ?: "Incorrect PIN")
        }
      }
    }
  }

  fun lockVault() {
    viewModelScope.launch {
      vaultRepository.lockVault()
      _uiState.update { it.copy(enteredPin = "", pinErrorMessage = null) }
    }
  }

  fun showAddNoteDialog(show: Boolean) {
    _uiState.update { it.copy(isAddNoteDialogVisible = show) }
  }

  fun addIncidentNote(title: String, content: String) {
    if (content.isBlank()) return
    viewModelScope.launch {
      vaultRepository.addIncidentNote(title, content)
      _uiState.update {
        it.copy(isAddNoteDialogVisible = false, feedbackMessage = "Encrypted incident note saved")
      }
    }
  }

  fun toggleAudioRecording() {
    if (_uiState.value.isRecordingAudio) {
      // Stop recording
      recordingJob?.cancel()
      val duration = _uiState.value.recordingSeconds
      viewModelScope.launch {
        vaultRepository.recordAudioEvidence("Emergency Ambient Recording (${duration}s)", duration)
        _uiState.update {
          it.copy(isRecordingAudio = false, recordingSeconds = 0, feedbackMessage = "Encrypted audio evidence saved")
        }
      }
    } else {
      // Start recording
      _uiState.update { it.copy(isRecordingAudio = true, recordingSeconds = 0) }
      recordingJob = viewModelScope.launch {
        while (true) {
          delay(1000)
          _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
        }
      }
    }
  }

  fun deleteEvidence(id: String) {
    viewModelScope.launch {
      vaultRepository.deleteEvidence(id)
      _uiState.update { it.copy(feedbackMessage = "Evidence removed") }
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    recordingJob?.cancel()
  }
}
