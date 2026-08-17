package com.example

import com.example.domain.model.HardwareTriggerType
import com.example.features.hardware.HardwareTriggerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareTriggerUnitTest {

  @Test
  fun testHardwareTrigger_TogglesAndSimulations() {
    val viewModel = HardwareTriggerViewModel()

    assertTrue(viewModel.uiState.value.config.isPowerTriplePressEnabled)
    assertTrue(viewModel.uiState.value.config.isVolumeShortcutEnabled)
    assertFalse(viewModel.uiState.value.config.isShakeTriggerEnabled)

    // Toggle shake
    viewModel.toggleShakeTrigger(true)
    assertTrue(viewModel.uiState.value.config.isShakeTriggerEnabled)

    // Simulate power button trigger
    viewModel.simulateHardwareTrigger(HardwareTriggerType.POWER_BUTTON_TRIPLE_PRESS)
    assertTrue(viewModel.uiState.value.triggerTriggeredSos)
    assertEquals(1, viewModel.uiState.value.recentTriggerEvents.size)
    assertEquals(HardwareTriggerType.POWER_BUTTON_TRIPLE_PRESS, viewModel.uiState.value.recentTriggerEvents.first().type)

    // Clear flag
    viewModel.clearSosTriggerFlag()
    assertFalse(viewModel.uiState.value.triggerTriggeredSos)
  }
}
