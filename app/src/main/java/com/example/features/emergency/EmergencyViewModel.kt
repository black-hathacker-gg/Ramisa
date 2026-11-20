package com.example.features.emergency

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.location.EmergencyLocationManager
import com.example.core.service.FusedLocationBackgroundService
import com.example.core.sms.EmergencySmsDispatcher
import com.example.core.sms.SmsDispatchReport
import com.example.data.local.AppDatabase
import com.example.data.repository.AppSessionManager
import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.EmergencyContact
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import com.example.domain.repository.EmergencyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmergencyUiState(
  val status: EmergencyStatus = EmergencyStatus.SOS_ACTIVE,
  val triggerType: TriggerType = TriggerType.SOS_BUTTON,
  val emergencyEventId: String = "EMG-1001",
  val elapsedSeconds: Int = 0,
  val location: GeoLocation = GeoLocation(),
  val locationLabel: String = "Acquiring Fused GPS Location...",
  val contactsNotifiedCount: Int = 0,
  val contactedNames: List<String> = emptyList(),
  val isSmsFallbackQueued: Boolean = true,
  val enteredPin: String = "",
  val pinErrorMessage: String? = null,
  val isResolveDialogVisible: Boolean = false,
  val isSmsSent: Boolean = false,
  val isSmsPermissionGranted: Boolean = true,
  val smsStatusSummary: String = "Broadcasting Emergency SMS with Fused GPS...",
  val emergencyContacts: List<EmergencyContact> = emptyList(),
  val primaryContact: EmergencyContact? = null,
  val isLiveLocationStreaming: Boolean = true,
  val nationalHotlineCallState: String? = null
)

class EmergencyViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val emergencyRepository: EmergencyRepository = EmergencyRepositoryImpl()
  private val db = AppDatabase.getDatabase(application)
  
  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  init {
    loadContacts()
    startLocationServiceAndListener()
    startEmergencyTimer()
  }

  private fun loadContacts() {
    viewModelScope.launch {
      db.emergencyContactDao().getAllContacts().collect { list ->
        _uiState.update {
          it.copy(
            emergencyContacts = list,
            primaryContact = list.firstOrNull(),
            contactsNotifiedCount = if (it.contactsNotifiedCount == 0) list.size else it.contactsNotifiedCount
          )
        }
      }
    }
  }

  private fun startLocationServiceAndListener() {
    val context = getApplication<Application>()
    // Start background fused location service
    try {
      FusedLocationBackgroundService.startLocationService(context)
    } catch (e: Exception) {
      // Ignored
    }

    viewModelScope.launch {
      // Fetch immediate accurate location
      val loc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
      _uiState.update {
        it.copy(
          location = loc,
          locationLabel = "${loc.addressName} (±${String.format("%.1f", loc.accuracyMeters)}m)"
        )
      }

      // Collect continuous updates
      EmergencyLocationManager.latestLocation.collect { updatedLoc ->
        if (updatedLoc != null) {
          _uiState.update {
            it.copy(
              location = updatedLoc,
              locationLabel = "${updatedLoc.addressName} (±${String.format(java.util.Locale.US, "%.1f", updatedLoc.accuracyMeters)}m)"
            )
          }
        }
      }
    }
  }

  fun refreshLocation() {
    val context = getApplication<Application>()
    viewModelScope.launch {
      val loc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
      _uiState.update {
        it.copy(
          location = loc,
          locationLabel = "${loc.addressName} (±${String.format(java.util.Locale.US, "%.1f", loc.accuracyMeters)}m)"
        )
      }
    }
  }

  private fun startEmergencyTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
      }
    }
  }

  fun dispatchSimSmsAndCall(customNote: String = "", initiateCall: Boolean = true) {
    val context = getApplication<Application>()
    
    viewModelScope.launch {
        // Schedule robust background SMS dispatch via WorkManager
        val accurateLoc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
        val data = androidx.work.workDataOf(
            "custom_note" to customNote,
            "lat" to accurateLoc.latitude,
            "lng" to accurateLoc.longitude
        )
        val smsWorkRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.core.sms.worker.EmergencySmsWorker>()
            .setInputData(data)
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(smsWorkRequest)

        // Update UI to reflect SMS is queued for background sending
        _uiState.update { current ->
            current.copy(
                isSmsSent = true,
                smsStatusSummary = "Emergency SMS queued for reliable background delivery..."
            )
        }

        // Automatically initiate calls sequentially
        if (initiateCall) {
            EmergencySmsDispatcher.initiateEmergencyPhoneCall(context, "999")
            delay(3000) // Small delay before next
            EmergencySmsDispatcher.initiateEmergencyPhoneCall(context, "10921")
        }
    }
  }

  fun triggerSos(triggerType: TriggerType = TriggerType.SOS_BUTTON, note: String = "", autoCall: Boolean = true) {
    viewModelScope.launch {
      val context = getApplication<Application>()
      val accurateLoc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
      
      val res = emergencyRepository.triggerSos(accurateLoc, triggerType, customNote = note)
      val event = res.getOrNull()
      val eventId = event?.id ?: "EMG-${System.currentTimeMillis() % 10000}"
      _uiState.update {
        it.copy(
          status = EmergencyStatus.SOS_ACTIVE,
          triggerType = triggerType,
          emergencyEventId = eventId,
          elapsedSeconds = 0,
          location = accurateLoc,
          pinErrorMessage = null
        )
      }
      emergencyRepository.dispatchAlerts(eventId, _uiState.value.contactsNotifiedCount, accurateLoc)
      dispatchSimSmsAndCall(customNote = note, initiateCall = autoCall)
    }
    startEmergencyTimer()
  }

  fun showResolveDialog(show: Boolean) {
    _uiState.update { it.copy(isResolveDialogVisible = show, enteredPin = "", pinErrorMessage = null) }
  }

  fun onPinEntered(pin: String) {
    _uiState.update { it.copy(enteredPin = pin, pinErrorMessage = null) }
  }

  fun resolveEmergency(onResolved: () -> Unit) {
    val userPin = AppSessionManager.currentUser.value?.emergencyPin ?: "1234"
    viewModelScope.launch {
      val res = emergencyRepository.resolveEmergency(
        eventId = _uiState.value.emergencyEventId,
        enteredPin = _uiState.value.enteredPin,
        expectedPin = userPin
      )
      if (res.isSuccess) {
        timerJob?.cancel()
        _uiState.update {
          it.copy(
            status = EmergencyStatus.RESOLVED,
            isResolveDialogVisible = false
          )
        }
        onResolved()
      } else {
        _uiState.update {
          it.copy(pinErrorMessage = res.exceptionOrNull()?.message ?: "Invalid Safety PIN. Try again.")
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
