package com.example.features.map

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.location.EmergencyLocationManager
import com.example.data.repository.DangerZonesRepositoryImpl
import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.GeoLocation
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class DangerMapUiState(
  val deviceLocation: GeoLocation? = null,
  val locationMessage: String? = null,
  val centeringLocation: Boolean = false,
  val selectedZone: DangerZone? = null,
  val searchQuery: String = "",
  val selectedRiskFilter: RiskLevel? = null,
  val selectedCategoryFilter: DangerCategory? = null,
  val allDangerZones: List<DangerZone> = emptyList(),
  val filteredDangerZones: List<DangerZone> = emptyList(),
  val nearestLocalReport: DangerZone? = null,
  val reportPoint: LatLng? = null,
  val reportSubmissionSuccess: Boolean = false,
  val reportError: String? = null,
  val savingReport: Boolean = false,
  val mapLayer: MapLayerType = MapLayerType.HEATMAP,
  val cameraAction: Pair<Long, MapCameraAction>? = null
)

enum class MapLayerType { HEATMAP, RISK_PINS, SAFE_CORRIDORS }

class DangerMapViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = DangerZonesRepositoryImpl(application)
  private val _uiState = MutableStateFlow(DangerMapUiState())
  val uiState: StateFlow<DangerMapUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) { repository.loadLocalReports() }
    viewModelScope.launch {
      repository.dangerZones.collect { zones ->
        update { it.copy(allDangerZones = zones) }
      }
    }
    viewModelScope.launch {
      EmergencyLocationManager.latestLocation.collect { location ->
        if (EmergencyLocationManager.hasLocationPermission(application)) {
          update { it.copy(deviceLocation = location?.takeIf(GeoLocation::isAvailable)) }
        }
      }
    }
  }

  fun refreshLocation(recenter: Boolean = true) {
    if (_uiState.value.centeringLocation) return
    update { it.copy(centeringLocation = true, locationMessage = null) }
    viewModelScope.launch {
      val location = EmergencyLocationManager.getAccurateEmergencyCoordinates(getApplication())
      update { state ->
        if (location.isAvailable) {
          state.copy(
            deviceLocation = location,
            centeringLocation = false,
            locationMessage = if (isFresh(location)) null else "Showing your last known location.",
            cameraAction = if (recenter) cameraAction(MapCameraAction.Center(LatLng(location.latitude, location.longitude), 13.0))
              else state.cameraAction
          )
        } else {
          state.copy(deviceLocation = null, centeringLocation = false, locationMessage = location.addressName)
        }
      }
    }
  }

  fun locationDenied() {
    update { it.copy(deviceLocation = null, locationMessage = "Enable location permission to see where you are.") }
  }

  fun selectZone(zone: DangerZone, recenter: Boolean = false) {
    update {
      it.copy(
        selectedZone = zone,
        cameraAction = if (recenter) cameraAction(MapCameraAction.Center(LatLng(zone.latitude, zone.longitude), 13.0))
          else it.cameraAction
      )
    }
  }

  fun setSearchQuery(query: String) = update { it.copy(searchQuery = query) }

  fun setRiskFilter(risk: RiskLevel?) =
    update { it.copy(selectedRiskFilter = risk.takeUnless { value -> value == it.selectedRiskFilter }) }

  fun setCategoryFilter(category: DangerCategory?) =
    update { it.copy(selectedCategoryFilter = category.takeUnless { value -> value == it.selectedCategoryFilter }) }

  fun setMapLayer(layer: MapLayerType) = update { it.copy(mapLayer = layer) }

  fun zoom(steps: Double) = update { it.copy(cameraAction = cameraAction(MapCameraAction.Zoom(steps))) }

  fun openReportDialog(point: LatLng) =
    update { it.copy(reportPoint = point, reportSubmissionSuccess = false, reportError = null) }

  fun closeReportDialog() {
    if (!_uiState.value.savingReport) update { it.copy(reportPoint = null) }
  }

  fun submitUnsafeSpot(name: String, area: String, reason: String) {
    val state = _uiState.value
    val point = state.reportPoint ?: return
    if (state.savingReport || name.isBlank() || reason.isBlank()) return
    update { it.copy(savingReport = true, reportError = null) }
    viewModelScope.launch(Dispatchers.IO) {
      val success = repository.reportNewUnsafeLocation(
        name.trim(), area.trim(), reason.trim(), point.latitude, point.longitude
      )
      update {
        it.copy(
          savingReport = false,
          reportSubmissionSuccess = success,
          reportError = if (success) null else "Couldn’t save this report on your device. Try again."
        )
      }
    }
  }

  private fun update(change: (DangerMapUiState) -> DangerMapUiState) {
    _uiState.update { current ->
      val next = change(current)
      val query = next.searchQuery.trim()
      val location = next.deviceLocation?.takeIf(::isFresh)
      val sorted = next.allDangerZones.sortedBy { zone ->
        location?.let { distanceKm(it, zone) } ?: 0.0
      }
      val filtered = sorted.filter { zone ->
        (next.selectedRiskFilter == null || zone.riskLevel == next.selectedRiskFilter) &&
          (next.selectedCategoryFilter == null || zone.category == next.selectedCategoryFilter) &&
          (query.isEmpty() || zone.name.contains(query, true) || zone.areaName.contains(query, true) ||
            zone.reportedIncidentsSummary.contains(query, true))
      }
      val nearby = location?.let { fix ->
        sorted.firstOrNull { it.isLocalReport && distanceKm(fix, it) <= 1.5 }
      }
      next.copy(
        filteredDangerZones = filtered,
        selectedZone = next.selectedZone?.takeIf { selected -> filtered.any { it.id == selected.id } },
        nearestLocalReport = nearby
      )
    }
  }

  private fun cameraAction(action: MapCameraAction): Pair<Long, MapCameraAction> =
    SystemClock.elapsedRealtimeNanos() to action

  internal fun isFresh(location: GeoLocation): Boolean =
    (location.locationAgeMillis ?: Long.MAX_VALUE) < 120_000L

  internal fun distanceLabel(location: GeoLocation, zone: DangerZone): String {
    val distance = (distanceKm(location, zone) * 10.0).roundToInt() / 10.0
    return "$distance km away"
  }

  private fun distanceKm(location: GeoLocation, zone: DangerZone): Double {
    val dLat = Math.toRadians(zone.latitude - location.latitude)
    val dLon = Math.toRadians(zone.longitude - location.longitude)
    val a = (sin(dLat / 2) * sin(dLat / 2) +
      cos(Math.toRadians(location.latitude)) * cos(Math.toRadians(zone.latitude)) *
      sin(dLon / 2) * sin(dLon / 2)).coerceIn(0.0, 1.0)
    return 6371.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
  }
}
