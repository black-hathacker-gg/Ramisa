package com.example.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DownloadState {
    NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED
}

data class MapRegion(
    val id: String,
    val name: String,
    val description: String,
    val sizeMB: Double,
    val state: DownloadState = DownloadState.NOT_DOWNLOADED,
    val progress: Float = 0f
)

data class OfflineMapsUiState(
    val regions: List<MapRegion> = listOf(
        MapRegion("1", "Dhaka - South", "Dhanmondi, Shahbagh, DU Campus, Lalbagh", 145.2, DownloadState.DOWNLOADED, 1f),
        MapRegion("2", "Dhaka - North", "Gulshan, Banani, Uttara, Mirpur", 182.5),
        MapRegion("3", "Chittagong City", "Agrabad, GEC, Halishahar", 110.8),
        MapRegion("4", "Sylhet City", "Zindabazar, Amberkhana, SUST", 85.4)
    ),
    val totalDownloadedMB: Double = 145.2
)

class OfflineMapsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OfflineMapsUiState())
    val uiState: StateFlow<OfflineMapsUiState> = _uiState.asStateFlow()

    fun startDownload(regionId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedRegions = state.regions.map {
                    if (it.id == regionId) it.copy(state = DownloadState.DOWNLOADING, progress = 0f) else it
                }
                state.copy(regions = updatedRegions)
            }

            // Simulate download progress
            for (i in 1..100) {
                delay(30) // Simulate network delay
                _uiState.update { state ->
                    val updatedRegions = state.regions.map {
                        if (it.id == regionId) it.copy(progress = i / 100f) else it
                    }
                    state.copy(regions = updatedRegions)
                }
            }

            _uiState.update { state ->
                val downloadedRegion = state.regions.find { it.id == regionId }
                val updatedTotal = state.totalDownloadedMB + (downloadedRegion?.sizeMB ?: 0.0)
                val updatedRegions = state.regions.map {
                    if (it.id == regionId) it.copy(state = DownloadState.DOWNLOADED, progress = 1f) else it
                }
                state.copy(regions = updatedRegions, totalDownloadedMB = updatedTotal)
            }
        }
    }

    fun deleteRegion(regionId: String) {
        _uiState.update { state ->
            val deletedRegion = state.regions.find { it.id == regionId }
            val updatedTotal = state.totalDownloadedMB - (deletedRegion?.sizeMB ?: 0.0)
            val updatedRegions = state.regions.map {
                if (it.id == regionId) it.copy(state = DownloadState.NOT_DOWNLOADED, progress = 0f) else it
            }
            state.copy(regions = updatedRegions, totalDownloadedMB = maxOf(0.0, updatedTotal))
        }
    }
}
