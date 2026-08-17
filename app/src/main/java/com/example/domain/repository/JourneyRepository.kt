package com.example.domain.repository

import com.example.domain.model.GeoLocation
import com.example.domain.model.Journey
import com.example.domain.model.TravelMode
import kotlinx.coroutines.flow.StateFlow

interface JourneyRepository {
  val activeJourney: StateFlow<Journey?>
  val journeyHistory: StateFlow<List<Journey>>
  val isDeviationDetected: StateFlow<Boolean>

  suspend fun startJourney(
    startLocation: String,
    destination: String,
    expectedArrivalMinutes: Int,
    travelMode: TravelMode,
    notes: String = ""
  ): Result<Journey>

  suspend fun updateLocation(location: GeoLocation): Result<Boolean>
  suspend fun completeJourney(journeyId: String): Result<Boolean>
  suspend fun cancelJourney(journeyId: String): Result<Boolean>
  suspend fun respondToSafetyCheck(safe: Boolean): Result<Boolean>
}
