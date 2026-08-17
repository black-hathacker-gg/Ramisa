package com.example.data.repository

import com.example.domain.model.GeoLocation
import com.example.domain.model.Journey
import com.example.domain.model.JourneyStatus
import com.example.domain.model.TravelMode
import com.example.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JourneyRepositoryImpl : JourneyRepository {

  private val _activeJourney = MutableStateFlow<Journey?>(null)
  override val activeJourney: StateFlow<Journey?> = _activeJourney.asStateFlow()

  private val _journeyHistory = MutableStateFlow<List<Journey>>(emptyList())
  override val journeyHistory: StateFlow<List<Journey>> = _journeyHistory.asStateFlow()

  private val _isDeviationDetected = MutableStateFlow(false)
  override val isDeviationDetected: StateFlow<Boolean> = _isDeviationDetected.asStateFlow()

  override suspend fun startJourney(
    startLocation: String,
    destination: String,
    expectedArrivalMinutes: Int,
    travelMode: TravelMode,
    notes: String
  ): Result<Journey> {
    val journey = Journey(
      id = "jrn_${System.currentTimeMillis() % 100000}",
      startLocation = startLocation.trim(),
      destination = destination.trim(),
      expectedArrivalMinutes = expectedArrivalMinutes,
      travelMode = travelMode,
      notes = notes.trim(),
      status = JourneyStatus.IN_PROGRESS,
      startTimeMillis = System.currentTimeMillis()
    )
    _activeJourney.value = journey
    _isDeviationDetected.value = false
    _journeyHistory.update { listOf(journey) + it }
    return Result.success(journey)
  }

  override suspend fun updateLocation(location: GeoLocation): Result<Boolean> {
    return Result.success(true)
  }

  override suspend fun completeJourney(journeyId: String): Result<Boolean> {
    _activeJourney.update { current ->
      if (current?.id == journeyId) current.copy(status = JourneyStatus.SAFE_ARRIVED) else current
    }
    val completed = _activeJourney.value
    _activeJourney.value = null
    if (completed != null) {
      _journeyHistory.update { list ->
        list.map { if (it.id == journeyId) completed else it }
      }
    }
    return Result.success(true)
  }

  override suspend fun cancelJourney(journeyId: String): Result<Boolean> {
    _activeJourney.update { current ->
      if (current?.id == journeyId) current.copy(status = JourneyStatus.CANCELLED) else current
    }
    _activeJourney.value = null
    return Result.success(true)
  }

  override suspend fun respondToSafetyCheck(safe: Boolean): Result<Boolean> {
    if (!safe) {
      _activeJourney.update { it?.copy(status = JourneyStatus.ALERT_TRIGGERED) }
    }
    return Result.success(safe)
  }
}
