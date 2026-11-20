package com.example.features.ridesafety

import android.os.SystemClock
import com.example.core.location.EmergencyLocationManager
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
  val feedbackMessage: String? = null
)

enum class TransportType(val label: String) {
  RICKSHAW("Rickshaw / Easybike (রিকশা)"),
  CNG("CNG Auto-Rickshaw (সিএনজি)"),
  BUS("Local / City Bus (বাস)"),
  RIDE_SHARE("Pathao / Uber Ride (রাইড)")
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
    if (_uiState.value.isRideMonitoringActive) return
    val startedAt = SystemClock.elapsedRealtime()

    _uiState.update {
      it.copy(
        isRideMonitoringActive = true,
        rideDurationMinutes = 0,
        isDeviationAlertActive = false,
        feedbackMessage = "Ride timer started. Use Share ride to send your details."
      )
    }

    monitorJob?.cancel()
    monitorJob = viewModelScope.launch {
      while (true) {
        delay(60000) // 1 minute ticker
        _uiState.update { s ->
          val newDur = ((SystemClock.elapsedRealtime() - startedAt) / 60_000L).toInt()
          val deviation = newDur >= s.maxExpectedMinutes
          s.copy(
            rideDurationMinutes = newDur,
            isDeviationAlertActive = deviation,
            feedbackMessage = if (deviation) "Overdue Arrival Alert: Exceeded estimated ride time!" else s.feedbackMessage
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
        feedbackMessage = "Ride marked complete."
      )
    }
  }

  fun simulateRouteDeviation() {
    _uiState.update {
      it.copy(
        isDeviationAlertActive = true,
        feedbackMessage = "Test alert: simulated route deviation."
      )
    }
  }

  fun clearFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  fun rideShareText(): String = buildString {
    val ride = _uiState.value
    appendLine("RAMISA ride check-in · ${ride.transportType.label}")
    if (ride.vehiclePlateNumber.isNotBlank()) appendLine("Vehicle: ${ride.vehiclePlateNumber.trim()}")
    if (ride.driverNameOrDescription.isNotBlank()) appendLine("Driver: ${ride.driverNameOrDescription.trim()}")
    if (ride.destination.isNotBlank()) appendLine("Destination: ${ride.destination.trim()}")
    appendLine("Expected ride duration: ${ride.maxExpectedMinutes} minutes")
    EmergencyLocationManager.latestLocation.value?.takeIf { it.isAvailable }?.let { point ->
      appendLine("Last recorded location: https://www.google.com/maps?q=${point.latitude},${point.longitude}")
    }
  }.trimEnd()
}
