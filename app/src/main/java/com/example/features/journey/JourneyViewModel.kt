package com.example.features.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
  val startLocation: String = "Mirpur 10, Dhaka",
  val destination: String = "Dhaka University Campus",
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
  private val journeyRepository: JourneyRepository = JourneyRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(JourneyUiState())
  val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null
  private var promptCountdownJob: Job? = null

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
  }

  fun onStartLocationChanged(loc: String) = _uiState.update { it.copy(startLocation = loc) }
  fun onDestinationChanged(dest: String) = _uiState.update { it.copy(destination = dest) }
  fun onDurationChanged(minutes: Int) = _uiState.update { it.copy(expectedDurationMinutes = minutes) }
  fun onTravelModeSelected(mode: TravelMode) = _uiState.update { it.copy(travelMode = mode) }
  fun onNotesChanged(notes: String) = _uiState.update { it.copy(notes = notes) }

  fun startJourney() {
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
            statusMessage = "Safe Journey started. Route tracking & proactive safety checks armed."
          )
        }
        startTrackingTimer()
      }
    }
  }

  private fun startTrackingTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _uiState.update { current ->
          val newSeconds = current.elapsedSeconds + 1
          val newMinutes = newSeconds / 60
          current.copy(elapsedSeconds = newSeconds, elapsedMinutes = newMinutes)
        }
      }
    }
  }

  fun completeJourney() {
    val journeyId = _uiState.value.activeJourney?.id ?: return
    viewModelScope.launch {
      journeyRepository.completeJourney(journeyId)
      timerJob?.cancel()
      promptCountdownJob?.cancel()
      _uiState.update {
        it.copy(
          isJourneyActive = false,
          isSafetyPromptVisible = false,
          statusMessage = "You have marked yourself safe. Journey completed."
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
            statusMessage = "Safety check timed out! SOS alert escalation prepared."
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
