package com.example.features.child

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChildZone(
  val id: String,
  val name: String,
  val type: String, // "School", "Home", "Coaching / Daycare", "Park"
  val address: String,
  val radiusMeters: Int,
  val isInside: Boolean,
  val lastCheckedTime: String
)

data class ChildSafetyUiState(
  val childName: String = "Anika (Age 9)",
  val batteryLevel: Int = 84,
  val isGpsActive: Boolean = true,
  val isOneTouchSosConfigured: Boolean = true,
  val isSafeZoneAlertActive: Boolean = true,
  val zones: List<ChildZone> = emptyList(),
  val emergencyCallNumber: String = "1098", // National Child Helpline BD
  val feedbackMessage: String? = null
)

class ChildSafetyViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(ChildSafetyUiState())
  val uiState: StateFlow<ChildSafetyUiState> = _uiState.asStateFlow()

  init {
    loadDefaultChildData()
  }

  private fun loadDefaultChildData() {
    _uiState.update {
      it.copy(
        zones = listOf(
          ChildZone(
            id = "cz_1",
            name = "Viqarunnisa Noon School & College",
            type = "School",
            address = "1/A New Bailey Road, Dhaka",
            radiusMeters = 250,
            isInside = true,
            lastCheckedTime = "Just now (01:45 PM)"
          ),
          ChildZone(
            id = "cz_2",
            name = "Home (Dhanmondi Lake View Apt)",
            type = "Home",
            address = "Road 8/A, Dhanmondi R/A, Dhaka",
            radiusMeters = 150,
            isInside = false,
            lastCheckedTime = "Departed at 07:30 AM"
          ),
          ChildZone(
            id = "cz_3",
            name = "Sunrise Swimming & Art Academy",
            type = "Coaching / Daycare",
            address = "Dhanmondi Club Ground, Dhaka",
            radiusMeters = 200,
            isInside = false,
            lastCheckedTime = "Scheduled at 04:30 PM"
          )
        )
      )
    }
  }

  fun toggleSafeZoneAlert(enabled: Boolean) {
    _uiState.update {
      it.copy(
        isSafeZoneAlertActive = enabled,
        feedbackMessage = if (enabled) "Safe Zone Geofencing alerts activated." else "Geofencing alerts paused."
      )
    }
  }

  fun triggerSimulatedChildSos() {
    _uiState.update {
      it.copy(
        feedbackMessage = "Simulated Child SOS received! Instant notification sent to Guardians and Helpline 1098."
      )
    }
  }

  fun updateChildName(name: String) {
    _uiState.update { it.copy(childName = name) }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
