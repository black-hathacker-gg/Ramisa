package com.example.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DangerZonesRepositoryImpl
import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import com.example.domain.repository.DangerZonesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DangerMapUiState(
  val userLatitude: Double = 23.7465, // Dhaka center (Dhanmondi / DU axis)
  val userLongitude: Double = 90.3750,
  val zoomLevel: Float = 1.0f,
  val selectedZone: DangerZone? = null,
  val searchQuery: String = "",
  val selectedRiskFilter: RiskLevel? = null,
  val selectedCategoryFilter: DangerCategory? = null,
  val allDangerZones: List<DangerZone> = emptyList(),
  val filteredDangerZones: List<DangerZone> = emptyList(),
  val isProximityAlertActive: Boolean = false,
  val nearestDangerZone: DangerZone? = null,
  val distanceToNearestKm: Double = 0.0,
  val showReportDialog: Boolean = false,
  val reportSubmissionSuccess: Boolean = false,
  val mapLayer: MapLayerType = MapLayerType.HEATMAP
)

enum class MapLayerType {
  HEATMAP,
  RISK_PINS,
  SAFE_CORRIDORS
}

class DangerMapViewModel(
  private val repository: DangerZonesRepository = DangerZonesRepositoryImpl()
) : ViewModel() {

  private val _uiState = MutableStateFlow(DangerMapUiState())
  val uiState: StateFlow<DangerMapUiState> = _uiState.asStateFlow()

  init {
    loadDangerZones()
  }

  private fun loadDangerZones() {
    viewModelScope.launch {
      val zones = repository.getNearbyDangerZones(_uiState.value.userLatitude, _uiState.value.userLongitude)
      _uiState.update { state ->
        val nearest = zones.minByOrNull { z ->
          calculateDistanceKm(state.userLatitude, state.userLongitude, z.latitude, z.longitude)
        }
        val dist = nearest?.let { calculateDistanceKm(state.userLatitude, state.userLongitude, it.latitude, it.longitude) } ?: 0.0
        state.copy(
          allDangerZones = zones,
          filteredDangerZones = zones,
          nearestDangerZone = nearest,
          distanceToNearestKm = dist,
          isProximityAlertActive = dist <= 1.5, // Alert if within 1.5 km of a danger zone
          selectedZone = nearest
        )
      }
    }
  }

  fun selectZone(zone: DangerZone?) {
    _uiState.update { it.copy(selectedZone = zone) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    applyFilters()
  }

  fun setRiskFilter(risk: RiskLevel?) {
    _uiState.update { it.copy(selectedRiskFilter = if (it.selectedRiskFilter == risk) null else risk) }
    applyFilters()
  }

  fun setCategoryFilter(category: DangerCategory?) {
    _uiState.update { it.copy(selectedCategoryFilter = if (it.selectedCategoryFilter == category) null else category) }
    applyFilters()
  }

  fun setMapLayer(layer: MapLayerType) {
    _uiState.update { it.copy(mapLayer = layer) }
  }

  fun zoomIn() {
    _uiState.update { it.copy(zoomLevel = (it.zoomLevel + 0.25f).coerceAtMost(2.5f)) }
  }

  fun zoomOut() {
    _uiState.update { it.copy(zoomLevel = (it.zoomLevel - 0.25f).coerceAtLeast(0.6f)) }
  }

  fun centerOnUser() {
    _uiState.update { it.copy(zoomLevel = 1.0f, selectedZone = it.nearestDangerZone) }
  }

  fun openReportDialog() {
    _uiState.update { it.copy(showReportDialog = true, reportSubmissionSuccess = false) }
  }

  fun closeReportDialog() {
    _uiState.update { it.copy(showReportDialog = false) }
  }

  fun submitUnsafeSpot(name: String, area: String, reason: String) {
    viewModelScope.launch {
      val success = repository.reportNewUnsafeLocation(
        name = name,
        area = area,
        reason = reason,
        lat = _uiState.value.userLatitude + 0.005,
        lng = _uiState.value.userLongitude + 0.005
      )
      if (success) {
        _uiState.update { it.copy(reportSubmissionSuccess = true) }
        loadDangerZones()
      }
    }
  }

  private fun applyFilters() {
    viewModelScope.launch {
      val results = repository.searchDangerZones(
        query = _uiState.value.searchQuery,
        riskFilter = _uiState.value.selectedRiskFilter,
        categoryFilter = _uiState.value.selectedCategoryFilter
      )
      _uiState.update { it.copy(filteredDangerZones = results) }
    }
  }

  fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return Math.round(r * c * 10.0) / 10.0
  }
}
