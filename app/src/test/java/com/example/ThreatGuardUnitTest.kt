package com.example

import com.example.core.audio.AcousticAlertGate
import com.example.core.audio.AcousticWindow
import com.example.core.audio.YamnetModel
import com.example.domain.model.AcousticCategoryScore
import com.example.domain.model.AudioInputSource
import com.example.features.threat.ThreatGuardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThreatGuardUnitTest {
  private val dispatcher = StandardTestDispatcher()
  @Before fun setUp() { Dispatchers.setMain(dispatcher) }
  @After fun tearDown() { Dispatchers.resetMain() }

  @Test fun isolatedScreamOrInterruptedSequenceDoesNotOfferAnAlert() {
    val gate = AcousticAlertGate()
    assertFalse(gate.observe(0.92f, 1_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.2f, 2_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.9f, 3_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.9f, 7_000, AudioInputSource.LIVE_MICROPHONE))
    assertTrue(gate.observe(0.9f, 8_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.9f, 9_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.9f, 10_000, AudioInputSource.LIVE_MICROPHONE))
  }

  @Test fun duplicateWindowsAndReplayScoresCannotPassTheLiveGate() {
    val gate = AcousticAlertGate()
    assertFalse(gate.observe(0.99f, 1_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.99f, 1_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.99f, 2_000, AudioInputSource.PRERECORDED_REPLAY))
    assertFalse(gate.observe(0.99f, 3_000, AudioInputSource.PRERECORDED_REPLAY))
    assertFalse(gate.observe(0.99f, 4_000, AudioInputSource.MANUAL_PREVIEW))
    assertFalse(gate.observe(Float.NaN, 5_000, AudioInputSource.LIVE_MICROPHONE))
    assertFalse(gate.observe(0.99f, 6_000, AudioInputSource.LIVE_MICROPHONE))
    assertTrue(gate.observe(0.99f, 7_000, AudioInputSource.LIVE_MICROPHONE))
  }

  @Test fun manualPreviewCannotRequestSosEvenWithOptInAndConfirmation() = runTest(dispatcher) {
    val model = ThreatGuardViewModel(RuntimeEnvironment.getApplication())
    assertFalse(model.uiState.value.isAutoSosEnabled)
    model.toggleAutoSos(true)
    model.previewConfirmation()
    assertEquals(AudioInputSource.MANUAL_PREVIEW, model.uiState.value.pendingConfirmationSource)
    assertNull(model.uiState.value.analysisResult.screamModelScore)
    model.confirmSos()
    assertFalse(model.consumeSosRequest())
    assertFalse(model.uiState.value.triggeredAutoSos)
  }

  @Test fun repeatedReplayResultsShowActualScoresButNeverOfferAnAlert() = runTest(dispatcher) {
    val model = ThreatGuardViewModel(RuntimeEnvironment.getApplication())
    model.toggleAutoSos(true)
    repeat(3) { index ->
      model.acceptWindow(AcousticWindow(listOf(AcousticCategoryScore("Screaming", 0.99f)),
        -22f, List(24) { 0.5f }, AudioInputSource.PRERECORDED_REPLAY, index * 1_000L, 20))
    }
    assertEquals(0.99f, model.uiState.value.analysisResult.screamModelScore!!, 0f)
    assertNull(model.uiState.value.pendingConfirmationSource)
    model.confirmSos()
    assertFalse(model.consumeSosRequest())
  }

  @Test fun expiredOrCancelledPromptCannotSend() = runTest(dispatcher) {
    val model = ThreatGuardViewModel(RuntimeEnvironment.getApplication())
    model.previewConfirmation()
    runCurrent()
    advanceTimeBy(15_001)
    runCurrent()
    assertNull(model.uiState.value.pendingConfirmationSource)
    assertFalse(model.consumeSosRequest())
    model.previewConfirmation()
    model.cancelConfirmation()
    model.confirmSos()
    assertFalse(model.consumeSosRequest())
  }

  @Test fun deniedPermissionDoesNotInventAnAnalysisOrStartTheMicrophone() {
    val model = ThreatGuardViewModel(RuntimeEnvironment.getApplication())
    model.onMicrophonePermissionDenied()
    assertFalse(model.uiState.value.isMonitoring)
    assertNotNull(model.uiState.value.errorMessage)
    assertNull(model.uiState.value.analysisResult.screamModelScore)
    assertFalse(model.consumeSosRequest())
  }

  @Test fun amplitudeIsCalculatedFromPcmRelativeToFullScale() {
    assertEquals(-120f, YamnetModel.rmsDbfs(FloatArray(100)), 0.001f)
    assertEquals(-6.0206f, YamnetModel.rmsDbfs(FloatArray(100) { 0.5f }), 0.001f)
    assertEquals(0f, YamnetModel.rmsDbfs(FloatArray(100) { 1f }), 0.001f)
  }
}
