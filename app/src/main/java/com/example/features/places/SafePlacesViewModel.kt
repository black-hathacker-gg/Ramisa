package com.example.features.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.location.EmergencyLocationManager
import com.example.data.repository.SafePlacesRepositoryImpl
import com.example.domain.model.PlaceType
import com.example.domain.model.SafePlace
import com.example.domain.repository.SafePlacesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SafePlacesUiState(
  val searchQuery: String = "",
  val selectedFilter: PlaceType? = null,
  val places: List<SafePlace> = emptyList(),
  val dialIntentNumber: String? = null,
  val hasFreshLocation: Boolean = false
)

class SafePlacesViewModel(
  private val repository: SafePlacesRepository = SafePlacesRepositoryImpl()
) : ViewModel() {

  private val controls = MutableStateFlow(SafePlacesUiState())

  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<SafePlacesUiState> = combine(
    controls, EmergencyLocationManager.latestLocation
  ) { filters, location ->
    filters to location
  }.mapLatest { (filters, location) ->
    val deviceFix = location?.takeIf { it.isAvailable && (it.locationAgeMillis ?: Long.MAX_VALUE) < 120_000L }
    val matches = repository.searchPlaces(filters.searchQuery, filters.selectedFilter)
    val displayed = if (deviceFix == null) {
      matches
    } else {
      val ids = matches.mapTo(mutableSetOf(), SafePlace::id)
      repository.getNearbyPlaces(deviceFix.latitude, deviceFix.longitude).filter { it.id in ids }
    }
    filters.copy(places = displayed, hasFreshLocation = deviceFix != null)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, SafePlacesUiState())

  fun onSearchQueryChanged(query: String) {
    controls.update { it.copy(searchQuery = query) }
  }

  fun onFilterSelected(type: PlaceType?) {
    controls.update { it.copy(selectedFilter = type.takeUnless { newType -> newType == it.selectedFilter }) }
  }

  fun onCallPlace(phoneNumber: String) {
    controls.update { it.copy(dialIntentNumber = phoneNumber) }
  }

  fun clearDialIntent() {
    controls.update { it.copy(dialIntentNumber = null) }
  }
}
