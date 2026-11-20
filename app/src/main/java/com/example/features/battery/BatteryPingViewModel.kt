package com.example.features.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BatteryPingUiState(
    val isEnabled: Boolean = false,
    val threshold: Int = 15,
    val recentPingEvents: List<String> = emptyList()
)

class BatteryPingViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BatteryPingUiState())
    val uiState: StateFlow<BatteryPingUiState> = _uiState.asStateFlow()

    init {
        BatteryMonitorManager.initialize(application)
        // Sync with manager
        _uiState.update { 
            it.copy(
                isEnabled = BatteryMonitorManager.isLowBatteryPingEnabled.value,
                threshold = BatteryMonitorManager.batteryThreshold.value
            ) 
        }
    }

    fun toggleAutoPing(enabled: Boolean) {
        BatteryMonitorManager.setLowBatteryPingEnabled(getApplication(), enabled)
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    fun updateThreshold(threshold: Float) {
        val intThreshold = threshold.toInt()
        BatteryMonitorManager.setBatteryThreshold(getApplication(), intThreshold)
        _uiState.update { it.copy(threshold = intThreshold) }
    }

    fun simulatePing() {
        _uiState.update { state ->
            val newEvent = "Preview at ${state.threshold}% battery. No SMS sent."
            state.copy(
                recentPingEvents = (listOf(newEvent) + state.recentPingEvents).take(5)
            )
        }
    }
}
