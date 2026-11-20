package com.example.features.volunteer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DemoVolunteer(
  val id: String,
  val name: String,
  val universityOrOrg: String,
  val showsIdBadge: Boolean,
  val ratingsCount: Int,
  val distanceMeters: Int,
  val isAvailableNow: Boolean
)

data class VolunteerEscortUiState(
  val pickupLocation: String = "TSC, Dhaka University",
  val destination: String = "Sufia Kamal Hall",
  val isEscortRequested: Boolean = false,
  val matchedVolunteer: DemoVolunteer? = null,
  val activeVolunteers: List<DemoVolunteer> = emptyList(),
  val feedbackMessage: String? = null
)

class VolunteerEscortViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(VolunteerEscortUiState())
  val uiState: StateFlow<VolunteerEscortUiState> = _uiState.asStateFlow()

  init {
    loadVolunteers()
  }

  private fun loadVolunteers() {
    _uiState.update {
      it.copy(
        activeVolunteers = listOf(
          DemoVolunteer(
            id = "v_1",
            name = "Volunteer A",
            universityOrOrg = "Campus volunteer",
            showsIdBadge = true,
            ratingsCount = 42,
            distanceMeters = 180,
            isAvailableNow = true
          ),
          DemoVolunteer(
            id = "v_2",
            name = "Volunteer B",
            universityOrOrg = "Student safety volunteer",
            showsIdBadge = true,
            ratingsCount = 29,
            distanceMeters = 350,
            isAvailableNow = true
          ),
          DemoVolunteer(
            id = "v_3",
            name = "Volunteer C",
            universityOrOrg = "Community volunteer",
            showsIdBadge = true,
            ratingsCount = 58,
            distanceMeters = 600,
            isAvailableNow = false
          )
        )
      )
    }
  }

  fun updatePickup(pickup: String) {
    _uiState.update { it.copy(pickupLocation = pickup) }
  }

  fun updateDestination(dest: String) {
    _uiState.update { it.copy(destination = dest) }
  }

  fun requestEscort() {
    val state = _uiState.value
    if (state.pickupLocation.isBlank() || state.destination.isBlank()) return
    val volunteer = state.activeVolunteers.firstOrNull { it.isAvailableNow }
    _uiState.update {
      it.copy(
        isEscortRequested = true,
        matchedVolunteer = volunteer,
        feedbackMessage = if (volunteer != null) "Practice match displayed. No escort was contacted." else "No example volunteers are available."
      )
    }
  }

  fun cancelEscort() {
    _uiState.update {
      it.copy(
        isEscortRequested = false,
        matchedVolunteer = null,
        feedbackMessage = "Practice run ended."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
