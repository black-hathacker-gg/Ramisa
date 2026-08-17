package com.example.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.TriggerType
import com.example.domain.repository.EmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
  val events: List<EmergencyEvent> = emptyList(),
  val isLoading: Boolean = false
)

class HistoryViewModel(
  private val emergencyRepository: EmergencyRepository = EmergencyRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(HistoryUiState())
  val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      emergencyRepository.emergencyHistory.collect { list ->
        _uiState.update { it.copy(events = list) }
      }
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      emergencyRepository.clearHistory()
      _uiState.update { it.copy(events = emptyList()) }
    }
  }
}
