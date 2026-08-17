package com.example.domain.model

enum class HardwareTriggerType {
  POWER_BUTTON_TRIPLE_PRESS,
  VOLUME_ROCKER_HOLD_OR_DOUBLE_TAP,
  SHAKE_GESTURE_DISCREET,
  POCKET_FALL_OR_IMPACT,
  WEARABLE_BLUETOOTH_BEACON
}

data class HardwareTriggerConfig(
  val isPowerTriplePressEnabled: Boolean = true,
  val isVolumeShortcutEnabled: Boolean = true,
  val isShakeTriggerEnabled: Boolean = false,
  val shakeSensitivity: Float = 2.5f, // g-force threshold
  val isHapticFeedbackEnabled: Boolean = true,
  val autoSilentModeOnTrigger: Boolean = true,
  val isWearCompanionConnected: Boolean = false,
  val wearDeviceName: String = "RAMISA Safety Band v1"
)

data class HardwareTriggerEvent(
  val id: String,
  val type: HardwareTriggerType,
  val timestamp: Long = System.currentTimeMillis(),
  val description: String,
  val descriptionBn: String,
  val simulated: Boolean = false
)
