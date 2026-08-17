package com.example.features.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SafetyTimerUiState(
  val isTimerActive: Boolean = false,
  val activityReason: String = "Walking alone through quiet road",
  val totalDurationSeconds: Int = 900, // 15 minutes default
  val remainingSeconds: Int = 900,
  val isTriggeredDueToTimeout: Boolean = false,
  val pinInput: String = "",
  val feedbackMessage: String? = null
)

class SafetyTimerViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(SafetyTimerUiState())
  val uiState: StateFlow<SafetyTimerUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  fun setActivityReason(reason: String) {
    _uiState.update { it.copy(activityReason = reason) }
  }

  fun setDurationMinutes(minutes: Int) {
    val seconds = minutes * 60
    _uiState.update { it.copy(totalDurationSeconds = seconds, remainingSeconds = seconds) }
  }

  fun startSafetyTimer(onTimeoutSosTrigger: () -> Unit) {
    _uiState.update {
      it.copy(
        isTimerActive = true,
        remainingSeconds = it.totalDurationSeconds,
        isTriggeredDueToTimeout = false,
        feedbackMessage = "Safety Check-in Timer activated. Please check in before time expires!"
      )
    }

    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isActive && _uiState.value.remainingSeconds > 0) {
        delay(1000)
        _uiState.update { state ->
          val next = state.remainingSeconds - 1
          state.copy(remainingSeconds = next)
        }
      }

      if (_uiState.value.isTimerActive && _uiState.value.remainingSeconds <= 0) {
        _uiState.update {
          it.copy(
            isTimerActive = false,
            isTriggeredDueToTimeout = true,
            feedbackMessage = "Safety Timer expired without check-in! Emergency SOS triggered."
          )
        }
        onTimeoutSosTrigger()
      }
    }
  }

  fun extendTimer(extraMinutes: Int = 10) {
    val extraSeconds = extraMinutes * 60
    _uiState.update {
      it.copy(
        remainingSeconds = it.remainingSeconds + extraSeconds,
        totalDurationSeconds = it.totalDurationSeconds + extraSeconds,
        feedbackMessage = "Extended timer by $extraMinutes minutes."
      )
    }
  }

  fun cancelSafetyTimer() {
    timerJob?.cancel()
    _uiState.update {
      it.copy(
        isTimerActive = false,
        remainingSeconds = it.totalDurationSeconds,
        feedbackMessage = "Safety check-in completed. Timer cancelled safely."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
