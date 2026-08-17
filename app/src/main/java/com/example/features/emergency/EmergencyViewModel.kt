package com.example.features.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppSessionManager
import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import com.example.domain.repository.EmergencyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmergencyUiState(
  val status: EmergencyStatus = EmergencyStatus.SOS_ACTIVE,
  val triggerType: TriggerType = TriggerType.SOS_BUTTON,
  val emergencyEventId: String = "EMG-1001",
  val elapsedSeconds: Int = 0,
  val location: GeoLocation = GeoLocation(),
  val locationLabel: String = "Road 11, Banani, Dhaka, Bangladesh (GPS Active)",
  val contactsNotifiedCount: Int = 3,
  val isSmsFallbackQueued: Boolean = true,
  val enteredPin: String = "",
  val pinErrorMessage: String? = null,
  val isResolveDialogVisible: Boolean = false,
  val isSmsSent: Boolean = true,
  val isLiveLocationStreaming: Boolean = true,
  val nationalHotlineCallState: String? = null
)

class EmergencyViewModel(
  private val emergencyRepository: EmergencyRepository = EmergencyRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  init {
    viewModelScope.launch {
      emergencyRepository.currentLocation.collect { loc ->
        _uiState.update {
          it.copy(
            location = loc,
            locationLabel = "${loc.addressName} (Accuracy ±${loc.accuracyMeters}m)"
          )
        }
      }
    }
    startEmergencyTimer()
  }

  private fun startEmergencyTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
      }
    }
  }

  fun triggerSos(triggerType: TriggerType = TriggerType.SOS_BUTTON, note: String = "") {
    viewModelScope.launch {
      val res = emergencyRepository.triggerSos(triggerType, customNote = note)
      val event = res.getOrNull()
      val eventId = event?.id ?: "EMG-${System.currentTimeMillis() % 10000}"

      _uiState.update {
        it.copy(
          status = EmergencyStatus.SOS_ACTIVE,
          triggerType = triggerType,
          emergencyEventId = eventId,
          elapsedSeconds = 0,
          pinErrorMessage = null
        )
      }

      // Dispatch alert network & SMS simulation
      emergencyRepository.dispatchAlerts(eventId, _uiState.value.contactsNotifiedCount, _uiState.value.location)
    }
    startEmergencyTimer()
  }

  fun showResolveDialog(show: Boolean) {
    _uiState.update { it.copy(isResolveDialogVisible = show, enteredPin = "", pinErrorMessage = null) }
  }

  fun onPinEntered(pin: String) {
    _uiState.update { it.copy(enteredPin = pin, pinErrorMessage = null) }
  }

  fun resolveEmergency(onResolved: () -> Unit) {
    val userPin = AppSessionManager.currentUser.value?.emergencyPin ?: "1234"
    viewModelScope.launch {
      val res = emergencyRepository.resolveEmergency(
        eventId = _uiState.value.emergencyEventId,
        enteredPin = _uiState.value.enteredPin,
        expectedPin = userPin
      )
      if (res.isSuccess) {
        timerJob?.cancel()
        _uiState.update {
          it.copy(
            status = EmergencyStatus.RESOLVED,
            isResolveDialogVisible = false
          )
        }
        onResolved()
      } else {
        _uiState.update {
          it.copy(pinErrorMessage = res.exceptionOrNull()?.message ?: "Invalid Safety PIN. Try again.")
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
