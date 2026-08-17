package com.example.features.volunteer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VerifiedVolunteer(
  val id: String,
  val name: String,
  val universityOrOrg: String,
  val studentIdVerified: Boolean,
  val ratingsCount: Int,
  val distanceMeters: Int,
  val isAvailableNow: Boolean,
  val phone: String
)

data class VolunteerEscortUiState(
  val pickupLocation: String = "TSC, Dhaka University",
  val destination: String = "Sufia Kamal Hall",
  val isEscortRequested: Boolean = false,
  val matchedVolunteer: VerifiedVolunteer? = null,
  val activeVolunteers: List<VerifiedVolunteer> = emptyList(),
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
          VerifiedVolunteer(
            id = "v_1",
            name = "Sadia Afrin",
            universityOrOrg = "Dhaka University (Rover Scout / NID Verified)",
            studentIdVerified = true,
            ratingsCount = 42,
            distanceMeters = 180,
            isAvailableNow = true,
            phone = "+8801700112233"
          ),
          VerifiedVolunteer(
            id = "v_2",
            name = "Farzana Yasmin",
            universityOrOrg = "BUET Women Safety Taskforce",
            studentIdVerified = true,
            ratingsCount = 29,
            distanceMeters = 350,
            isAvailableNow = true,
            phone = "+8801811223344"
          ),
          VerifiedVolunteer(
            id = "v_3",
            name = "Nusrat Jahan",
            universityOrOrg = "Red Crescent Youth Volunteer",
            studentIdVerified = true,
            ratingsCount = 58,
            distanceMeters = 600,
            isAvailableNow = false,
            phone = "+8801911223344"
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
    val volunteer = _uiState.value.activeVolunteers.firstOrNull { it.isAvailableNow }
    _uiState.update {
      it.copy(
        isEscortRequested = true,
        matchedVolunteer = volunteer,
        feedbackMessage = if (volunteer != null) "Verified volunteer ${volunteer.name} accepted your escort request!" else "Searching for verified escorts nearby..."
      )
    }
  }

  fun cancelEscort() {
    _uiState.update {
      it.copy(
        isEscortRequested = false,
        matchedVolunteer = null,
        feedbackMessage = "Escort request concluded safely."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
