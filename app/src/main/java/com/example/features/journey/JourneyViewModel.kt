package com.example.features.journey

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.sms.EmergencySmsDispatcher
import com.example.data.repository.JourneyRepositoryImpl
import com.example.domain.model.Journey
import com.example.domain.model.JourneyStatus
import com.example.domain.model.TravelMode
import com.example.domain.repository.JourneyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JourneyUiState(
  val startLocation: String = "",
  val destination: String = "",
  val expectedDurationMinutes: Int = 45,
  val travelMode: TravelMode = TravelMode.RICKSHAW,
  val notes: String = "",
  val activeJourney: Journey? = null,
  val isJourneyActive: Boolean = false,
  val elapsedMinutes: Int = 0,
  val elapsedSeconds: Int = 0,
  val isSafetyPromptVisible: Boolean = false,
  val promptCountdownSeconds: Int = 30,
  val isDeviationAlertTriggered: Boolean = false,
  val statusMessage: String? = null
)

class JourneyViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val journeyRepository: JourneyRepository = JourneyRepositoryImpl()

  private val _uiState = MutableStateFlow(JourneyUiState())
  val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null
  private var promptCountdownJob: Job? = null
  private var arrivalCheckShown = false

  init {
    viewModelScope.launch {
      journeyRepository.activeJourney.collect { journey ->
        _uiState.update {
          it.copy(
            activeJourney = journey,
            isJourneyActive = journey != null && journey.status == JourneyStatus.IN_PROGRESS
          )
        }
      }
    }

    viewModelScope.launch {
      com.example.core.location.EmergencyLocationManager.latestLocation.collect { loc ->
        if (loc?.isAvailable == true) {
          if (_uiState.value.isJourneyActive) journeyRepository.updateLocation(loc)
          else _uiState.update { state -> if (state.startLocation.isBlank()) state.copy(startLocation = loc.description()) else state }
        }
      }
    }
  }

  fun useCurrentGpsLocation() {
    viewModelScope.launch {
      val loc = com.example.core.location.EmergencyLocationManager.getAccurateEmergencyCoordinates(getApplication())
      _uiState.update {
        if (loc.isAvailable) it.copy(startLocation = loc.description(), statusMessage = null)
        else it.copy(statusMessage = "Location unavailable. Enter your starting point or enable device location.")
      }
    }
  }

  fun onStartLocationChanged(loc: String) = _uiState.update { it.copy(startLocation = loc) }
  fun onDestinationChanged(dest: String) = _uiState.update { it.copy(destination = dest) }
  fun onDurationChanged(minutes: Int) = _uiState.update { it.copy(expectedDurationMinutes = minutes) }
  fun onTravelModeSelected(mode: TravelMode) = _uiState.update { it.copy(travelMode = mode) }
  fun onNotesChanged(notes: String) = _uiState.update { it.copy(notes = notes) }

  fun startJourney() {
    if (_uiState.value.isJourneyActive) return
    if (_uiState.value.startLocation.isBlank() || _uiState.value.destination.isBlank()) {
      _uiState.update { it.copy(statusMessage = "Enter a starting point and destination.") }
      return
    }
    viewModelScope.launch {
      val res = journeyRepository.startJourney(
        startLocation = _uiState.value.startLocation,
        destination = _uiState.value.destination,
        expectedArrivalMinutes = _uiState.value.expectedDurationMinutes,
        travelMode = _uiState.value.travelMode,
        notes = _uiState.value.notes
      )
      if (res.isSuccess) {
        _uiState.update {
          it.copy(
            elapsedMinutes = 0,
            elapsedSeconds = 0,
            isSafetyPromptVisible = false,
            isDeviationAlertTriggered = false,
            statusMessage = "Journey started on this device. You’ll get a safety check at the expected arrival time."
          )
        }
        startTrackingTimer()
      }
    }
  }

  private fun startTrackingTimer() {
    timerJob?.cancel()
    val startedAt = SystemClock.elapsedRealtime()
    arrivalCheckShown = false
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        val elapsedSeconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt()
        _uiState.update { current ->
          current.copy(elapsedSeconds = elapsedSeconds, elapsedMinutes = elapsedSeconds / 60)
        }
        if (!arrivalCheckShown && _uiState.value.elapsedMinutes >= _uiState.value.expectedDurationMinutes) {
          arrivalCheckShown = true
          triggerSafetyCheckPrompt(true)
        }
      }
    }
  }

  fun completeJourney() {
    val journeyId = _uiState.value.activeJourney?.id ?: return
    val destination = _uiState.value.destination
    viewModelScope.launch {
      if (journeyRepository.completeJourney(journeyId).getOrNull() != true) return@launch
      timerJob?.cancel()
      promptCountdownJob?.cancel()
      
      // Auto dispatch "Reached Safely" SMS to guardians
      val smsSent = EmergencySmsDispatcher.dispatchSafeArrivalSms(
        context = getApplication(),
        destination = destination
      )

      val confirmationMsg = if (smsSent) {
        "Arrival SMS was accepted for sending. Check RAMISA for delivery status."
      } else {
        "You have marked yourself safe. Journey completed."
      }

      _uiState.update {
        it.copy(
          isJourneyActive = false,
          isSafetyPromptVisible = false,
          statusMessage = confirmationMsg
        )
      }
    }
  }

  fun cancelJourney() {
    val journeyId = _uiState.value.activeJourney?.id ?: return
    viewModelScope.launch {
      journeyRepository.cancelJourney(journeyId)
      timerJob?.cancel()
      promptCountdownJob?.cancel()
      _uiState.update {
        it.copy(
          isJourneyActive = false,
          isSafetyPromptVisible = false,
          statusMessage = "Journey cancelled."
        )
      }
    }
  }

  fun triggerSafetyCheckPrompt(show: Boolean) {
    _uiState.update { it.copy(isSafetyPromptVisible = show, promptCountdownSeconds = 30) }
    if (show) {
      promptCountdownJob?.cancel()
      promptCountdownJob = viewModelScope.launch {
        for (i in 30 downTo 1) {
          _uiState.update { it.copy(promptCountdownSeconds = i) }
          delay(1000)
        }
        // If countdown expires without response, trigger alert flag
        _uiState.update {
          it.copy(
            isDeviationAlertTriggered = true,
            isSafetyPromptVisible = false,
            statusMessage = "Safety check timed out. Open SOS if you need to alert your contacts."
          )
        }
      }
    } else {
      promptCountdownJob?.cancel()
    }
  }

  fun confirmSafetyCheck(safe: Boolean, onTriggerSos: (() -> Unit)? = null) {
    promptCountdownJob?.cancel()
    viewModelScope.launch {
      journeyRepository.respondToSafetyCheck(safe)
      if (safe) {
        _uiState.update {
          it.copy(
            isSafetyPromptVisible = false,
            statusMessage = "Thank you! Safety status confirmed."
          )
        }
      } else {
        _uiState.update {
          it.copy(
            isSafetyPromptVisible = false,
            isDeviationAlertTriggered = true
          )
        }
        onTriggerSos?.invoke()
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
    promptCountdownJob?.cancel()
  }
}
