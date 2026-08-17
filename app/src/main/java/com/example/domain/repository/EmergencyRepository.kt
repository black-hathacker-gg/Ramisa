package com.example.domain.repository

import com.example.domain.model.AlertDispatchResult
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import kotlinx.coroutines.flow.StateFlow

interface EmergencyRepository {
  val activeEmergency: StateFlow<EmergencyEvent?>
  val emergencyHistory: StateFlow<List<EmergencyEvent>>
  val currentLocation: StateFlow<GeoLocation>

  suspend fun triggerSos(
    triggerType: TriggerType,
    customNote: String = "",
    sendSilentAlarm: Boolean = false
  ): Result<EmergencyEvent>

  suspend fun dispatchAlerts(
    eventId: String,
    contactsCount: Int,
    location: GeoLocation
  ): Result<AlertDispatchResult>

  suspend fun resolveEmergency(
    eventId: String,
    enteredPin: String,
    expectedPin: String
  ): Result<Boolean>

  suspend fun triggerTestSos(): Result<EmergencyEvent>
  suspend fun clearHistory(): Boolean
}
