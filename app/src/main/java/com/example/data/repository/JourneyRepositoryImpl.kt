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
    if (!location.isAvailable || _activeJourney.value == null) return Result.success(false)
    _activeJourney.update { it?.copy(lastLocation = location) }
    return Result.success(true)
  }

  override suspend fun completeJourney(journeyId: String): Result<Boolean> {
    return finishJourney(journeyId, JourneyStatus.SAFE_ARRIVED)
  }

  override suspend fun cancelJourney(journeyId: String): Result<Boolean> {
    return finishJourney(journeyId, JourneyStatus.CANCELLED)
  }

  private fun finishJourney(journeyId: String, status: JourneyStatus): Result<Boolean> {
    val active = _activeJourney.value?.takeIf { it.id == journeyId } ?: return Result.success(false)
    if (!_activeJourney.compareAndSet(active, null)) return Result.success(false)
    _journeyHistory.update { entries -> entries.map { if (it.id == journeyId) active.copy(status = status) else it } }
    return Result.success(true)
  }

  override suspend fun respondToSafetyCheck(safe: Boolean): Result<Boolean> {
    _isDeviationDetected.value = !safe
    return Result.success(safe)
  }
}
