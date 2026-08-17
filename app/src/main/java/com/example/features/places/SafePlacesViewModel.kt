package com.example.features.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SafePlacesRepositoryImpl
import com.example.domain.model.PlaceType
import com.example.domain.model.SafePlace
import com.example.domain.repository.SafePlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafePlacesUiState(
  val searchQuery: String = "",
  val selectedFilter: PlaceType? = null,
  val places: List<SafePlace> = emptyList(),
  val isLoading: Boolean = false,
  val userLatitude: Double = 23.7465,
  val userLongitude: Double = 90.3750,
  val dialIntentNumber: String? = null
)

class SafePlacesViewModel(
  private val safePlacesRepository: SafePlacesRepository = SafePlacesRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(SafePlacesUiState())
  val uiState: StateFlow<SafePlacesUiState> = _uiState.asStateFlow()

  init {
    loadPlaces()
  }

  fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    filterPlaces()
  }

  fun onFilterSelected(type: PlaceType?) {
    _uiState.update { it.copy(selectedFilter = if (it.selectedFilter == type) null else type) }
    filterPlaces()
  }

  private fun loadPlaces() {
    viewModelScope.launch {
      val list = safePlacesRepository.getNearbyPlaces(_uiState.value.userLatitude, _uiState.value.userLongitude)
      _uiState.update { it.copy(places = list) }
    }
  }

  private fun filterPlaces() {
    viewModelScope.launch {
      val results = safePlacesRepository.searchPlaces(
        query = _uiState.value.searchQuery,
        filterType = _uiState.value.selectedFilter
      )
      _uiState.update { it.copy(places = results) }
    }
  }

  fun onCallPlace(phoneNumber: String) {
    _uiState.update { it.copy(dialIntentNumber = phoneNumber) }
  }

  fun clearDialIntent() {
    _uiState.update { it.copy(dialIntentNumber = null) }
  }
}
