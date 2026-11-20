package com.example.features.emergency

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.location.EmergencyLocationManager
import com.example.core.sms.EmergencySmsDispatcher
import com.example.core.sms.SmsDispatchReport
import com.example.data.local.AppDatabase
import com.example.data.local.toDomain
import com.example.data.repository.AppSessionManager
import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.EmergencyContact
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class EmergencyUiState(
  val status: EmergencyStatus = EmergencyStatus.IDLE,
  val triggerType: TriggerType = TriggerType.SOS_BUTTON,
  val emergencyEventId: String = "",
  val elapsedSeconds: Int = 0,
  val location: GeoLocation = GeoLocation(),
  val locationLabel: String = "Location unavailable",
  val contactsNotifiedCount: Int = 0,
  val contactedNames: List<String> = emptyList(),
  val isSmsFallbackQueued: Boolean = false,
  val enteredPin: String = "",
  val pinErrorMessage: String? = null,
  val isResolveDialogVisible: Boolean = false,
  val isSmsSent: Boolean = false,
  val isSmsPermissionGranted: Boolean = false,
  val smsStatusSummary: String = "No emergency SMS has been sent from this screen.",
  val emergencyContacts: List<EmergencyContact> = emptyList(),
  val primaryContact: EmergencyContact? = null,
  val isLiveLocationStreaming: Boolean = false,
  val nationalHotlineCallState: String? = null,
  val isDispatching: Boolean = false,
  val deliveredCount: Int = 0,
  val unknownCount: Int = 0,
  val lastReport: SmsDispatchReport? = null
)

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {
  private val emergencyRepository = EmergencyRepositoryImpl.shared
  private val db = AppDatabase.getDatabase(application)
  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState = _uiState.asStateFlow()
  private var timerJob: Job? = null
  private var dispatchJob: Job? = null

  init {
    refreshPermissions()
    viewModelScope.launch {
      db.emergencyContactDao().getAllContacts().collect { list ->
        _uiState.update { it.copy(emergencyContacts = list, primaryContact = list.firstOrNull { contact -> contact.callEnabled }) }
      }
    }
    viewModelScope.launch {
      EmergencySmsDispatcher.reports.collect { reports ->
        reports[_uiState.value.emergencyEventId]?.let(::applyReport)
      }
    }
    viewModelScope.launch {
      EmergencyLocationManager.latestLocation.collect { location ->
        if (location != null) _uiState.update { it.copy(location = location,
          locationLabel = location.description(), isLiveLocationStreaming = false) }
      }
    }
  }

  fun refreshPermissions() {
    val granted = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    _uiState.update { it.copy(isSmsPermissionGranted = granted) }
  }

  fun refreshLocation() {
    viewModelScope.launch {
      _uiState.update { it.copy(locationLabel = "Requesting device location…") }
      val location = withTimeoutOrNull(7_000L) {
        EmergencyLocationManager.getAccurateEmergencyCoordinates(getApplication())
      } ?: GeoLocation()
      _uiState.update { it.copy(location = location, locationLabel = location.description()) }
    }
  }

  /** Legacy name retained for callers. No automatic call is made regardless of initiateCall. */
  @Suppress("UNUSED_PARAMETER")
  fun dispatchSimSmsAndCall(customNote: String = "", initiateCall: Boolean = false) {
    triggerSos(note = customNote, autoCall = false)
  }

  /** Call only after an explicit user confirmation or a previously configured safety trigger. */
  @Suppress("UNUSED_PARAMETER")
  fun triggerSos(triggerType: TriggerType = TriggerType.SOS_BUTTON, note: String = "", autoCall: Boolean = false) {
    if (dispatchJob?.isActive == true || _uiState.value.isSmsFallbackQueued) return
    dispatchJob = viewModelScope.launch {
      refreshPermissions()
      _uiState.update { it.copy(isDispatching = true, isSmsSent = false, contactsNotifiedCount = 0,
        contactedNames = emptyList(), deliveredCount = 0, unknownCount = 0, lastReport = null,
        smsStatusSummary = "Preparing your confirmed SMS request…") }
      try {
        val event = emergencyRepository.triggerSos(_uiState.value.location.takeIf { it.isAvailable }, triggerType, note).getOrThrow()
        _uiState.update { it.copy(status = EmergencyStatus.SOS_ACTIVE, triggerType = triggerType,
          emergencyEventId = event.id, elapsedSeconds = 0, pinErrorMessage = null) }
        startEmergencyTimer()
        val report = EmergencySmsDispatcher.dispatchEmergencySms(getApplication(), customNote = note,
          location = _uiState.value.location.takeIf { it.isAvailable }, eventId = event.id)
        applyReport(report)
      } catch (cancelled: CancellationException) { throw cancelled }
      catch (_: Exception) {
        _uiState.update { it.copy(smsStatusSummary = "SMS outcome could not be verified. No automatic retry will be made.", unknownCount = 1) }
      } finally { _uiState.update { it.copy(isDispatching = false) } }
    }
  }

  private fun applyReport(report: SmsDispatchReport) {
    emergencyRepository.recordSmsConfirmation(report.eventId, report.successCount)
    _uiState.update { current -> current.copy(
      contactsNotifiedCount = report.successCount, contactedNames = report.successfulContactNames,
      isSmsFallbackQueued = report.pendingCount > 0, isSmsSent = report.successCount > 0,
      isSmsPermissionGranted = report.isPermissionGranted, smsStatusSummary = report.statusSummary,
      deliveredCount = report.deliveredCount, unknownCount = report.unknownCount, lastReport = report,
      location = report.locationUsed ?: current.location,
      locationLabel = report.locationUsed?.description() ?: current.locationLabel,
      status = if (current.status == EmergencyStatus.RESOLVED) current.status else if (report.successCount > 0)
        EmergencyStatus.CONTACTS_NOTIFIED else EmergencyStatus.SOS_ACTIVE
    ) }
  }

  private fun startEmergencyTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch { while (true) { delay(1_000L)
      _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
    } }
  }
  fun showResolveDialog(show: Boolean) { _uiState.update { it.copy(isResolveDialogVisible = show, enteredPin = "", pinErrorMessage = null) } }
  fun onPinEntered(pin: String) { _uiState.update { it.copy(enteredPin = pin, pinErrorMessage = null) } }
  fun resolveEmergency(onResolved: () -> Unit) {
    viewModelScope.launch {
      val pin = try { db.userDao().getProfileSync()?.toDomain()?.emergencyPin }
        catch (_: Exception) { null } ?: AppSessionManager.currentUser.value?.emergencyPin.orEmpty()
      val result = emergencyRepository.resolveEmergency(_uiState.value.emergencyEventId, _uiState.value.enteredPin, pin)
      if (result.isSuccess) {
        timerJob?.cancel()
        _uiState.update { it.copy(status = EmergencyStatus.RESOLVED, isResolveDialogVisible = false) }
        onResolved()
      } else _uiState.update { it.copy(pinErrorMessage = result.exceptionOrNull()?.message ?: "Invalid safety PIN") }
    }
  }
  override fun onCleared() { timerJob?.cancel(); super.onCleared() }
}
