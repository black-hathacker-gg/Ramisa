package com.example

import com.example.domain.model.ThreatLevel
import com.example.features.threat.ThreatGuardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThreatGuardUnitTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testThreatGuard_ToggleAndAutoSos() = runTest(testDispatcher) {
    val viewModel = ThreatGuardViewModel()

    assertFalse(viewModel.uiState.value.isMonitoring)
    viewModel.startMonitoring()
    assertTrue(viewModel.uiState.value.isMonitoring)

    // Simulate Threat Spike
    viewModel.simulateThreatSpike()
    assertEquals(ThreatLevel.CRITICAL, viewModel.uiState.value.analysisResult.threatLevel)
    assertTrue(viewModel.uiState.value.triggeredAutoSos)
    assertTrue(viewModel.uiState.value.recentDetections.isNotEmpty())

    viewModel.stopMonitoring()
    assertFalse(viewModel.uiState.value.isMonitoring)
  }
}
