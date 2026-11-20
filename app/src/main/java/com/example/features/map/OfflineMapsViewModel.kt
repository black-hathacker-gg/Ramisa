package com.example.features.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition

enum class DownloadState {
  NOT_DOWNLOADED, DOWNLOADING, PAUSED, DOWNLOADED, DELETING
}

data class MapRegion(
  val id: String,
  val name: String,
  val description: String,
  val bounds: LatLngBounds,
  val downloadedBytes: Long = 0L,
  val state: DownloadState = DownloadState.NOT_DOWNLOADED,
  val progress: Float? = null
)

data class OfflineMapsUiState(
  val regions: List<MapRegion> = listOf(
    MapRegion("dhaka-south", "Dhaka South", "Dhanmondi, Shahbagh, Dhaka University, Lalbagh",
      LatLngBounds.from(23.785, 90.465, 23.675, 90.300)),
    MapRegion("dhaka-north", "Dhaka North", "Gulshan, Banani, Uttara, Mirpur",
      LatLngBounds.from(23.932, 90.477, 23.760, 90.317)),
    MapRegion("chattogram", "Chattogram", "Agrabad, GEC, Halishahar",
      LatLngBounds.from(22.440, 91.873, 22.255, 91.720)),
    MapRegion("sylhet", "Sylhet", "Zindabazar, Amberkhana, SUST",
      LatLngBounds.from(24.960, 91.950, 24.845, 91.800))
  ),
  val activeRegionId: String? = null,
  val loading: Boolean = true,
  val error: String? = null
)

class OfflineMapsViewModel(application: Application) : AndroidViewModel(application) {
  private val _uiState = MutableStateFlow(OfflineMapsUiState())
  val uiState = _uiState.asStateFlow()
  private val stored = mutableMapOf<String, OfflineRegion>()
  private var closed = false
  private val offline = run {
    RamisaBaseMap.initialize(application)
    OfflineManager.getInstance(application)
  }.also {
    it.setOfflineMapboxTileCountLimit(3000)
  }

  init {
    refresh()
  }

  fun refresh() {
    if (_uiState.value.activeRegionId != null) return
    _uiState.update { it.copy(loading = true, error = null) }
    offline.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
      override fun onList(offlineRegions: Array<OfflineRegion>?) {
        if (closed) return
        stored.clear()
        for (saved in offlineRegions.orEmpty()) {
          val id = saved.metadata.toString(Charsets.UTF_8)
          if (_uiState.value.regions.none { it.id == id }) continue
          stored[id] = saved
          loadStatus(id, saved)
        }
        _uiState.update { it.copy(loading = false) }
      }

      override fun onError(error: String) {
        _uiState.update { it.copy(loading = false, error = "Saved maps could not be loaded.") }
      }
    })
  }

  fun startDownload(regionId: String) {
    val state = _uiState.value
    val region = state.regions.firstOrNull { it.id == regionId } ?: return
    if (state.loading || state.activeRegionId != null ||
      region.state !in setOf(DownloadState.NOT_DOWNLOADED, DownloadState.PAUSED)) return
    _uiState.update { it.copy(activeRegionId = regionId, error = null) }
    changeRegion(regionId) { it.copy(state = DownloadState.DOWNLOADING) }
    val saved = stored[regionId]
    if (saved != null) {
      beginDownload(regionId, saved)
      return
    }
    // Four small city extents at ≤14 zoom keep each pack comfortably below OpenFreeMap limits.
    val definition = OfflineTilePyramidRegionDefinition(
      RamisaBaseMap.styleUrl, region.bounds, 9.0, 14.0,
      getApplication<Application>().resources.displayMetrics.density
    )
    offline.createOfflineRegion(definition, regionId.toByteArray(Charsets.UTF_8),
      object : OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
          if (closed) return
          stored[regionId] = offlineRegion
          beginDownload(regionId, offlineRegion)
        }

        override fun onError(error: String) {
          changeRegion(regionId) { it.copy(state = DownloadState.NOT_DOWNLOADED) }
          finish(regionId, "Couldn’t start this map download. Try again.")
        }
      })
  }

  fun pauseDownload(regionId: String) {
    val saved = stored[regionId] ?: return
    if (_uiState.value.activeRegionId != regionId) return
    saved.setObserver(null)
    saved.setDownloadState(OfflineRegion.STATE_INACTIVE)
    changeRegion(regionId) { it.copy(state = DownloadState.PAUSED) }
    finish(regionId)
  }

  fun deleteRegion(regionId: String) {
    val saved = stored[regionId] ?: return
    if (_uiState.value.activeRegionId == regionId) return
    val region = _uiState.value.regions.first { it.id == regionId }
    if (region.state !in setOf(DownloadState.DOWNLOADED, DownloadState.PAUSED)) return
    changeRegion(regionId) { it.copy(state = DownloadState.DELETING) }
    saved.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
      override fun onDelete() {
        stored.remove(regionId)
        changeRegion(regionId) { it.copy(state = DownloadState.NOT_DOWNLOADED, downloadedBytes = 0L, progress = null) }
      }

      override fun onError(error: String) {
        changeRegion(regionId) { region }
        _uiState.update { it.copy(error = "Couldn’t remove this offline map. Try again.") }
      }
    })
  }

  private fun loadStatus(id: String, saved: OfflineRegion) {
    saved.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
      override fun onStatus(status: OfflineRegionStatus?) {
        if (status == null) {
          onError(null)
          return
        }
        if (_uiState.value.activeRegionId == id) return
        updateStatus(id, status, if (status.isComplete) DownloadState.DOWNLOADED else DownloadState.PAUSED)
      }

      override fun onError(error: String?) {
        changeRegion(id) { it.copy(state = DownloadState.PAUSED) }
        _uiState.update { it.copy(error = "Couldn’t read an offline map’s status.") }
      }
    })
  }

  private fun beginDownload(id: String, saved: OfflineRegion) {
    saved.setObserver(object : OfflineRegion.OfflineRegionObserver {
      override fun onStatusChanged(status: OfflineRegionStatus) {
        if (_uiState.value.activeRegionId != id || closed) return
        when {
          status.completedResourceSize >= 42_000_000 || status.completedTileCount >= 2600 ->
            failDownload(id, "This map reached its download size limit. Remove the partial download.")
          status.isComplete -> {
            updateStatus(id, status, DownloadState.DOWNLOADED)
            saved.setObserver(null)
            saved.setDownloadState(OfflineRegion.STATE_INACTIVE)
            finish(id)
          }
          else -> updateStatus(id, status, DownloadState.DOWNLOADING)
        }
      }

      override fun onError(error: OfflineRegionError) {
        failDownload(id, "Map download paused. Check your internet connection and resume.")
      }

      override fun mapboxTileCountLimitExceeded(limit: Long) {
        failDownload(id, "Offline map storage is full. Remove a saved map and retry.")
      }
    })
    saved.setDownloadState(OfflineRegion.STATE_ACTIVE)
  }

  private fun failDownload(id: String, message: String) {
    if (_uiState.value.activeRegionId != id) return
    pauseDownload(id)
    _uiState.update { it.copy(error = message) }
  }

  private fun updateStatus(id: String, status: OfflineRegionStatus, downloadState: DownloadState) {
    val progress = when {
      status.isComplete -> 1f
      !status.isRequiredResourceCountPrecise || status.requiredResourceCount <= 0 -> null
      else -> (status.completedResourceCount.toFloat() / status.requiredResourceCount).coerceIn(0f, 1f)
    }
    changeRegion(id) { it.copy(state = downloadState, progress = progress, downloadedBytes = status.completedResourceSize) }
  }

  private fun finish(regionId: String, error: String? = null) {
    _uiState.update {
      if (it.activeRegionId == regionId) it.copy(activeRegionId = null, error = error) else it
    }
  }

  private fun changeRegion(id: String, change: (MapRegion) -> MapRegion) {
    _uiState.update { state ->
      state.copy(regions = state.regions.map { if (it.id == id) change(it) else it })
    }
  }

  override fun onCleared() {
    _uiState.value.activeRegionId?.let(::pauseDownload)
    closed = true
    super.onCleared()
  }
}
