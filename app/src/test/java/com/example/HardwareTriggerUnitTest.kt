package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.HardwareTriggerType
import com.example.features.hardware.HardwareTriggerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HardwareTriggerUnitTest {

  @Test
  fun testHardwareTrigger_TogglesAndSimulations() {
    val viewModel = HardwareTriggerViewModel(ApplicationProvider.getApplicationContext<Application>())

    assertTrue(viewModel.uiState.value.config.isPowerTriplePressEnabled)
    assertFalse(viewModel.uiState.value.config.isVolumeShortcutEnabled)
    assertFalse(viewModel.uiState.value.config.isShakeTriggerEnabled)

    // Toggle shake
    viewModel.toggleShakeTrigger(true)
    assertTrue(viewModel.uiState.value.config.isShakeTriggerEnabled)
    viewModel.toggleVolumeShortcut(true)
    assertTrue(HardwareTriggerViewModel(ApplicationProvider.getApplicationContext<Application>())
      .uiState.value.config.isVolumeShortcutEnabled)

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
