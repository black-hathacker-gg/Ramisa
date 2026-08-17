package com.example

import com.example.data.repository.JourneyRepositoryImpl
import com.example.domain.model.TravelMode
import com.example.features.journey.JourneyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JourneyUnitTest {

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
  fun testJourneyViewModel_StartAndComplete() = runTest(testDispatcher) {
    val repository = JourneyRepositoryImpl()
    val viewModel = JourneyViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isJourneyActive)

    viewModel.onStartLocationChanged("Dhanmondi 27")
    viewModel.onDestinationChanged("TSC, University of Dhaka")
    viewModel.onTravelModeSelected(TravelMode.RICKSHAW)
    viewModel.onDurationChanged(30)

    viewModel.startJourney()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.isJourneyActive)
    assertNotNull(viewModel.uiState.value.activeJourney)
    assertEquals("Dhanmondi 27", viewModel.uiState.value.activeJourney?.startLocation)

    // Complete journey
    viewModel.completeJourney()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isJourneyActive)
  }

  @Test
  fun testJourneyViewModel_SafetyCheckPrompt() = runTest(testDispatcher) {
    val repository = JourneyRepositoryImpl()
    val viewModel = JourneyViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.startJourney()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.triggerSafetyCheckPrompt(true)
    assertTrue(viewModel.uiState.value.isSafetyPromptVisible)

    var sosTriggered = false
    viewModel.confirmSafetyCheck(safe = false, onTriggerSos = { sosTriggered = true })
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(sosTriggered)
    assertFalse(viewModel.uiState.value.isSafetyPromptVisible)
    assertTrue(viewModel.uiState.value.isDeviationAlertTriggered)
  }
}
