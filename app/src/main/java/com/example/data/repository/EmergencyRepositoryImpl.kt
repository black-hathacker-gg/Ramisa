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
import java.util.UUID

/** Local event state only. SMS submission and receipts belong to EmergencySmsDispatcher. */
class EmergencyRepositoryImpl : EmergencyRepository {
  companion object {
    val shared: EmergencyRepositoryImpl by lazy { EmergencyRepositoryImpl() }
  }
  private val _currentLocation = MutableStateFlow(GeoLocation())
  override val currentLocation: StateFlow<GeoLocation> = _currentLocation.asStateFlow()
  private val _activeEmergency = MutableStateFlow<EmergencyEvent?>(null)
  override val activeEmergency: StateFlow<EmergencyEvent?> = _activeEmergency.asStateFlow()
  private val _emergencyHistory = MutableStateFlow<List<EmergencyEvent>>(emptyList())
  override val emergencyHistory: StateFlow<List<EmergencyEvent>> = _emergencyHistory.asStateFlow()

  override suspend fun triggerSos(location: GeoLocation?, triggerType: TriggerType, customNote: String,
    sendSilentAlarm: Boolean): Result<EmergencyEvent> {
    val actual = location?.takeIf { it.isAvailable }
    _currentLocation.value = actual ?: GeoLocation()
    val event = EmergencyEvent(id = "EMG-${UUID.randomUUID()}", triggerType = triggerType,
      locationLabel = actual?.addressName ?: "Location unavailable", status = EmergencyStatus.SOS_ACTIVE,
      notes = customNote.ifBlank { if (sendSilentAlarm) "Silent SOS requested" else "SOS requested" })
    _activeEmergency.value = event
    _emergencyHistory.update { listOf(event) + it }
    // No implicit public/community broadcast: trusted-contact SMS is the separately confirmed action.
    return Result.success(event)
  }

  override suspend fun dispatchAlerts(eventId: String, contactsCount: Int, location: GeoLocation): Result<AlertDispatchResult> =
    Result.success(AlertDispatchResult(eventId, smsSentCount = 0, pushDeliveredCount = 0,
      nationalHotlineDialed = false,
      locationSharedUrl = if (location.isAvailable) "https://maps.google.com/?q=${location.latitude},${location.longitude}" else "",
      isOfflineQueued = false))

  fun recordSmsConfirmation(eventId: String, sentCount: Int) {
    if (sentCount <= 0) return
    _activeEmergency.update { if (it?.id == eventId && it.status != EmergencyStatus.RESOLVED)
      it.copy(status = EmergencyStatus.CONTACTS_NOTIFIED) else it }
    _emergencyHistory.update { entries -> entries.map {
      if (it.id == eventId && it.status != EmergencyStatus.RESOLVED) it.copy(status = EmergencyStatus.CONTACTS_NOTIFIED) else it
    } }
  }

  override suspend fun resolveEmergency(eventId: String, enteredPin: String, expectedPin: String): Result<Boolean> {
    if (expectedPin.isBlank()) return Result.failure(IllegalArgumentException("Set your safety PIN in your profile first"))
    if (enteredPin != expectedPin) return Result.failure(IllegalArgumentException("Invalid safety PIN"))
    if (_activeEmergency.value?.id == eventId) _activeEmergency.value = null
    _emergencyHistory.update { entries -> entries.map {
      if (it.id == eventId) it.copy(status = EmergencyStatus.RESOLVED) else it
    } }
    return Result.success(true)
  }

  override suspend fun triggerTestSos(): Result<EmergencyEvent> {
    val event = EmergencyEvent("TST-${UUID.randomUUID()}", TriggerType.TEST_SOS,
      locationLabel = _currentLocation.value.addressName, status = EmergencyStatus.RESOLVED,
      notes = "Local interface test only. No SMS, call, push or delivery test performed.")
    _emergencyHistory.update { listOf(event) + it }
    return Result.success(event)
  }
  override suspend fun clearHistory(): Boolean { _emergencyHistory.value = emptyList(); return true }
}
