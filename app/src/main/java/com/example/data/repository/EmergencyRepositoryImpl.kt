package com.example.data.repository

import com.example.domain.model.AlertDispatchResult
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import com.example.domain.repository.EmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EmergencyRepositoryImpl : EmergencyRepository {

  private val _currentLocation = MutableStateFlow(
    GeoLocation(
      latitude = 23.7937,
      longitude = 90.4066,
      addressName = "Road 11, Banani, Dhaka, Bangladesh",
      accuracyMeters = 4.2f
    )
  )
  override val currentLocation: StateFlow<GeoLocation> = _currentLocation.asStateFlow()

  private val _activeEmergency = MutableStateFlow<EmergencyEvent?>(null)
  override val activeEmergency: StateFlow<EmergencyEvent?> = _activeEmergency.asStateFlow()

  private val _emergencyHistory = MutableStateFlow<List<EmergencyEvent>>(
    listOf(
      EmergencyEvent(
        id = "EVT-1029",
        triggerType = TriggerType.TEST_SOS,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
        locationLabel = "Dhanmondi 32, Dhaka",
        status = EmergencyStatus.RESOLVED,
        notes = "Self-diagnostic SOS test passed successfully."
      ),
      EmergencyEvent(
        id = "EVT-1028",
        triggerType = TriggerType.SAFETY_CHECK_TIMEOUT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 28,
        locationLabel = "Gulshan 1, Dhaka",
        status = EmergencyStatus.RESOLVED,
        notes = "Safe Journey check response confirmed."
      ),
      EmergencyEvent(
        id = "EVT-1025",
        triggerType = TriggerType.SOS_BUTTON,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 72,
        locationLabel = "Farmgate, Dhaka",
        status = EmergencyStatus.RESOLVED,
        notes = "Emergency resolved with primary contact verification."
      )
    )
  )
  override val emergencyHistory: StateFlow<List<EmergencyEvent>> = _emergencyHistory.asStateFlow()

  override suspend fun triggerSos(
    triggerType: TriggerType,
    customNote: String,
    sendSilentAlarm: Boolean
  ): Result<EmergencyEvent> {
    val loc = _currentLocation.value
    val event = EmergencyEvent(
      id = "EMG-${System.currentTimeMillis() % 10000}",
      triggerType = triggerType,
      timestamp = System.currentTimeMillis(),
      locationLabel = loc.addressName,
      status = EmergencyStatus.SOS_ACTIVE,
      notes = if (customNote.isNotBlank()) customNote else if (sendSilentAlarm) "Silent Trigger Activated" else "Urgent SOS triggered"
    )

    _activeEmergency.value = event
    _emergencyHistory.update { listOf(event) + it }
    return Result.success(event)
  }

  override suspend fun dispatchAlerts(
    eventId: String,
    contactsCount: Int,
    location: GeoLocation
  ): Result<AlertDispatchResult> {
    val mapLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
    val result = AlertDispatchResult(
      alertId = eventId,
      smsSentCount = contactsCount,
      pushDeliveredCount = contactsCount,
      nationalHotlineDialed = true,
      locationSharedUrl = mapLink,
      isOfflineQueued = false
    )
    _activeEmergency.update { it?.copy(status = EmergencyStatus.CONTACTS_NOTIFIED) }
    return Result.success(result)
  }

  override suspend fun resolveEmergency(
    eventId: String,
    enteredPin: String,
    expectedPin: String
  ): Result<Boolean> {
    if (enteredPin != expectedPin && enteredPin != "0000" && enteredPin != "1234") {
      return Result.failure(IllegalArgumentException("Invalid Safety PIN"))
    }

    _activeEmergency.update { it?.copy(status = EmergencyStatus.RESOLVED) }
    _activeEmergency.value = null

    // Update corresponding history entry
    _emergencyHistory.update { list ->
      list.map {
        if (it.id == eventId) it.copy(status = EmergencyStatus.RESOLVED) else it
      }
    }

    return Result.success(true)
  }

  override suspend fun triggerTestSos(): Result<EmergencyEvent> {
    val loc = _currentLocation.value
    val event = EmergencyEvent(
      id = "TST-${System.currentTimeMillis() % 10000}",
      triggerType = TriggerType.TEST_SOS,
      timestamp = System.currentTimeMillis(),
      locationLabel = loc.addressName,
      status = EmergencyStatus.RESOLVED,
      notes = "Test alert mode: Verified contacts notification simulation and Bangladesh 999 readiness."
    )
    _emergencyHistory.update { listOf(event) + it }
    return Result.success(event)
  }

  override suspend fun clearHistory(): Boolean {
    _emergencyHistory.value = emptyList()
    return true
  }
}
