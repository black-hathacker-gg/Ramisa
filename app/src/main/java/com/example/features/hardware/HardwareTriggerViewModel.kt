package com.example.features.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.HardwareTriggerConfig
import com.example.domain.model.HardwareTriggerEvent
import com.example.domain.model.HardwareTriggerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HardwareTriggerUiState(
  val config: HardwareTriggerConfig = HardwareTriggerConfig(),
  val recentTriggerEvents: List<HardwareTriggerEvent> = emptyList(),
  val isTestingShake: Boolean = false,
  val currentGForce: Float = 1.0f,
  val triggerTriggeredSos: Boolean = false
)

class HardwareTriggerViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(HardwareTriggerUiState())
  val uiState: StateFlow<HardwareTriggerUiState> = _uiState.asStateFlow()

  fun togglePowerTriplePress(enabled: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(isPowerTriplePressEnabled = enabled)) }
  }

  fun toggleVolumeShortcut(enabled: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(isVolumeShortcutEnabled = enabled)) }
  }

  fun toggleShakeTrigger(enabled: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(isShakeTriggerEnabled = enabled)) }
  }

  fun updateShakeSensitivity(sensitivity: Float) {
    _uiState.update { it.copy(config = it.config.copy(shakeSensitivity = sensitivity)) }
  }

  fun toggleHapticFeedback(enabled: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(isHapticFeedbackEnabled = enabled)) }
  }

  fun toggleAutoSilentMode(enabled: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(autoSilentModeOnTrigger = enabled)) }
  }

  fun toggleWearCompanion(connected: Boolean) {
    _uiState.update { it.copy(config = it.config.copy(isWearCompanionConnected = connected)) }
  }

  fun simulateHardwareTrigger(type: HardwareTriggerType) {
    val event = when (type) {
      HardwareTriggerType.POWER_BUTTON_TRIPLE_PRESS -> HardwareTriggerEvent(
        id = "evt_" + System.currentTimeMillis(),
        type = type,
        description = "Power Button 3x Quick Press Signal Received",
        descriptionBn = "পাওয়ার বাটনে ৩ বার দ্রুত চাপ শনাক্ত হয়েছে",
        simulated = true
      )
      HardwareTriggerType.VOLUME_ROCKER_HOLD_OR_DOUBLE_TAP -> HardwareTriggerEvent(
        id = "evt_" + System.currentTimeMillis(),
        type = type,
        description = "Discreet In-Pocket Volume Rocker Trigger Confirmed",
        descriptionBn = "পকেটের ভেতর ভলিউম বাটন দীর্ঘ চাপ শনাক্ত হয়েছে",
        simulated = true
      )
      HardwareTriggerType.SHAKE_GESTURE_DISCREET -> HardwareTriggerEvent(
        id = "evt_" + System.currentTimeMillis(),
        type = type,
        description = "High-Distress Rapid Shake (>2.5G) Detected",
        descriptionBn = "দ্রুত ঝাঁকুনি সংকেত (>২.৫G) শনাক্ত হয়েছে",
        simulated = true
      )
      HardwareTriggerType.WEARABLE_BLUETOOTH_BEACON -> HardwareTriggerEvent(
        id = "evt_" + System.currentTimeMillis(),
        type = type,
        description = "RAMISA Safety Smart Band SOS Button Pressed",
        descriptionBn = "স্মার্টব্যান্ড থেকে এসওএস বাটন চাপ শনাক্ত হয়েছে",
        simulated = true
      )
      HardwareTriggerType.POCKET_FALL_OR_IMPACT -> HardwareTriggerEvent(
        id = "evt_" + System.currentTimeMillis(),
        type = type,
        description = "Severe Fall or Sudden Impact Telemetry Captured",
        descriptionBn = "আকস্মিক পতন বা আঘাত সংকেত শনাক্ত হয়েছে",
        simulated = true
      )
    }

    _uiState.update {
      it.copy(
        recentTriggerEvents = (listOf(event) + it.recentTriggerEvents).take(8),
        triggerTriggeredSos = true
      )
    }
  }

  fun clearSosTriggerFlag() {
    _uiState.update { it.copy(triggerTriggeredSos = false) }
  }
}
