package com.example.features.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.location.EmergencyLocationManager
import com.example.data.repository.AppSessionManager
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
  val userProfile: UserProfile = UserProfile(),
  val emergencyStatus: EmergencyStatus = EmergencyStatus.IDLE,
  val activeContactCount: Int = 3,
  val isLocationReady: Boolean = false,
  val isNetworkConnected: Boolean = true,
  val currentLocation: GeoLocation? = null,
  val isGpsHardwareEnabled: Boolean = true,
  val isRefreshingLocation: Boolean = false,
  val lastSafetyCheckTime: String = "Today, 1:45 PM"
)

class HomeViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      AppSessionManager.currentUser.collect { user ->
        _uiState.update { current ->
          current.copy(userProfile = user ?: UserProfile())
        }
      }
    }

    viewModelScope.launch {
      EmergencyLocationManager.latestLocation.collect { loc ->
        _uiState.update { current ->
          current.copy(
            currentLocation = loc,
            isLocationReady = loc != null
          )
        }
      }
    }

    viewModelScope.launch {
      EmergencyLocationManager.isGpsHardwareEnabled.collect { enabled ->
        _uiState.update { it.copy(isGpsHardwareEnabled = enabled) }
      }
    }
  }

  fun refreshLocation(context: Context) {
    viewModelScope.launch {
      _uiState.update { it.copy(isRefreshingLocation = true) }
      EmergencyLocationManager.startBackgroundLocationUpdates(context)
      val loc = EmergencyLocationManager.getAccurateEmergencyCoordinates(context)
      _uiState.update {
        it.copy(
          currentLocation = loc,
          isLocationReady = true,
          isRefreshingLocation = false
        )
      }
    }
  }

  fun triggerQuickCheck() {
    viewModelScope.launch {
      _uiState.update { it.copy(lastSafetyCheckTime = "Just now") }
    }
  }

  fun setSafetyStatus(status: EmergencyStatus) {
    _uiState.update { it.copy(emergencyStatus = status) }
  }
}
