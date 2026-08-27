package com.example.features.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CircleMemberStatus
import com.example.domain.model.GuardianCircleMember
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GuardianCircleUiState(
  val circleName: String = "Family & Close Friends",
  val myStatus: CircleMemberStatus = CircleMemberStatus.SAFE,
  val myBatteryLevel: Int = 88,
  val myLocation: String = "Dhanmondi 27, Dhaka",
  val members: List<GuardianCircleMember> = emptyList(),
  val isIntervalCheckinActive: Boolean = false,
  val checkinIntervalMinutes: Int = 10,
  val nextCheckinSecondsRemaining: Int = 600,
  val isAddMemberDialogOpen: Boolean = false,
  val statusMessage: String? = null
)

class GuardianCircleViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(GuardianCircleUiState())
  val uiState: StateFlow<GuardianCircleUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  init {
    loadDefaultCircleMembers()
  }

  private fun loadDefaultCircleMembers() {
    _uiState.update {
      it.copy(
        members = emptyList()
      )
    }
  }

  fun updateMyStatus(newStatus: CircleMemberStatus) {
    _uiState.update {
      it.copy(
        myStatus = newStatus,
        statusMessage = "Status updated to: ${newStatus.label}"
      )
    }
  }

  fun requestCheckin(memberId: String) {
    _uiState.update { state ->
      val updated = state.members.map { m ->
        if (m.id == memberId) m.copy(isCheckinRequested = true) else m
      }
      state.copy(
        members = updated,
        statusMessage = "Discreet 'Are you safe?' check-in ping sent!"
      )
    }
  }

  fun toggleIntervalCheckin(enabled: Boolean, intervalMinutes: Int = 10) {
    timerJob?.cancel()
    if (enabled) {
      val totalSeconds = intervalMinutes * 60
      _uiState.update {
        it.copy(
          isIntervalCheckinActive = true,
          checkinIntervalMinutes = intervalMinutes,
          nextCheckinSecondsRemaining = totalSeconds,
          statusMessage = "Night Walk safety timer active! Auto-prompt every $intervalMinutes mins"
        )
      }

      timerJob = viewModelScope.launch {
        while (true) {
          delay(1000)
          _uiState.update { s ->
            if (s.nextCheckinSecondsRemaining <= 1) {
              s.copy(
                nextCheckinSecondsRemaining = totalSeconds,
                statusMessage = "⚠️ Safety Check-In due! Confirm you are safe."
              )
            } else {
              s.copy(nextCheckinSecondsRemaining = s.nextCheckinSecondsRemaining - 1)
            }
          }
        }
      }
    } else {
      _uiState.update {
        it.copy(
          isIntervalCheckinActive = false,
          statusMessage = "Safety interval timer stopped."
        )
      }
    }
  }

  fun confirmSafeNow() {
    val totalSeconds = _uiState.value.checkinIntervalMinutes * 60
    _uiState.update {
      it.copy(
        myStatus = CircleMemberStatus.SAFE,
        nextCheckinSecondsRemaining = totalSeconds,
        statusMessage = "Confirmed: You checked in as SAFE."
      )
    }
  }

  fun openAddMemberDialog() {
    _uiState.update { it.copy(isAddMemberDialogOpen = true) }
  }

  fun closeAddMemberDialog() {
    _uiState.update { it.copy(isAddMemberDialogOpen = false) }
  }

  fun addMember(name: String, relation: String, phone: String) {
    val newMember = GuardianCircleMember(
      id = "mem_${System.currentTimeMillis()}",
      name = name,
      relation = relation,
      phone = phone,
      status = CircleMemberStatus.SAFE,
      batteryLevel = 100,
      lastLocation = "Location shared",
      lastUpdatedTime = "Just now"
    )
    _uiState.update {
      it.copy(
        members = it.members + newMember,
        isAddMemberDialogOpen = false,
        statusMessage = "$name added to Guardian Circle."
      )
    }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusMessage = null) }
  }
}
