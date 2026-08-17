package com.example.features.ridesafety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RideSafetyUiState(
  val transportType: TransportType = TransportType.RICKSHAW,
  val vehiclePlateNumber: String = "",
  val driverNameOrDescription: String = "",
  val destination: String = "",
  val isRideMonitoringActive: Boolean = false,
  val rideDurationMinutes: Int = 0,
  val maxExpectedMinutes: Int = 20,
  val isDeviationAlertActive: Boolean = false,
  val shareableRideLink: String? = null,
  val feedbackMessage: String? = null
)

enum class TransportType(val label: String, val iconBengali: String) {
  RICKSHAW("Rickshaw / Easybike (রিকশা)", "🛺"),
  CNG("CNG Auto-Rickshaw (সিএনজি)", "🚕"),
  BUS("Local / City Bus (বাস)", "🚌"),
  RIDE_SHARE("Pathao / Uber Ride (রাইড)", "🚗")
}

class RideSafetyViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(RideSafetyUiState())
  val uiState: StateFlow<RideSafetyUiState> = _uiState.asStateFlow()

  private var monitorJob: Job? = null

  fun setTransportType(type: TransportType) {
    _uiState.update { it.copy(transportType = type) }
  }

  fun updateVehiclePlate(plate: String) {
    _uiState.update { it.copy(vehiclePlateNumber = plate) }
  }

  fun updateDriverInfo(driver: String) {
    _uiState.update { it.copy(driverNameOrDescription = driver) }
  }

  fun updateDestination(dest: String) {
    _uiState.update { it.copy(destination = dest) }
  }

  fun updateExpectedTime(minutes: Int) {
    _uiState.update { it.copy(maxExpectedMinutes = minutes) }
  }

  fun startRideMonitoring() {
    val plate = _uiState.value.vehiclePlateNumber.ifBlank { "Unspecified Vehicle" }
    val dest = _uiState.value.destination.ifBlank { "Destination" }
    val type = _uiState.value.transportType.label
    val link = "https://ramisa-safety.live/ride/${System.currentTimeMillis()}?dest=${dest.replace(" ", "+")}"

    _uiState.update {
      it.copy(
        isRideMonitoringActive = true,
        rideDurationMinutes = 0,
        isDeviationAlertActive = false,
        shareableRideLink = link,
        feedbackMessage = "Ride Guard active for $type. Guardian link generated & shared!"
      )
    }

    monitorJob?.cancel()
    monitorJob = viewModelScope.launch {
      while (true) {
        delay(60000) // 1 minute ticker
        _uiState.update { s ->
          val newDur = s.rideDurationMinutes + 1
          val deviation = newDur > s.maxExpectedMinutes
          s.copy(
            rideDurationMinutes = newDur,
            isDeviationAlertActive = deviation,
            feedbackMessage = if (deviation) "⚠️ Overdue Arrival Alert: Exceeded estimated ride time!" else s.feedbackMessage
          )
        }
      }
    }
  }

  fun endRideMonitoring() {
    monitorJob?.cancel()
    _uiState.update {
      it.copy(
        isRideMonitoringActive = false,
        isDeviationAlertActive = false,
        feedbackMessage = "Ride completed safely! Guardians notified."
      )
    }
  }

  fun simulateRouteDeviation() {
    _uiState.update {
      it.copy(
        isDeviationAlertActive = true,
        feedbackMessage = "⚠️ Route Deviation Detected! Vehicle moved off standard path."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
