package com.example

import com.example.data.repository.SafePlacesRepositoryImpl
import com.example.domain.model.PlaceType
import com.example.features.fakecall.FakeCallState
import com.example.features.fakecall.FakeCallViewModel
import com.example.features.places.SafePlacesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SafePlacesAndFakeCallUnitTest {

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
  fun testSafePlaces_SearchAndFilter() = runTest(testDispatcher) {
    val repository = SafePlacesRepositoryImpl()
    val viewModel = SafePlacesViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.places.isNotEmpty())

    // Search for "Dhanmondi"
    viewModel.onSearchQueryChanged("Dhanmondi")
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.places.any { it.name.contains("Dhanmondi", ignoreCase = true) })

    // Filter by POLICE_STATION
    viewModel.onFilterSelected(PlaceType.POLICE_STATION)
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.places.all { it.type == PlaceType.POLICE_STATION })
  }

  @Test
  fun testFakeCall_Lifecycle() = runTest(testDispatcher) {
    val viewModel = FakeCallViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(FakeCallState.IDLE, viewModel.uiState.value.state)

    // Trigger immediate fake call
    viewModel.setDelaySeconds(0)
    viewModel.triggerCallSchedule()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(FakeCallState.RINGING, viewModel.uiState.value.state)

    // Accept call
    viewModel.acceptCall()
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(FakeCallState.CONNECTED, viewModel.uiState.value.state)

    // End call
    viewModel.declineOrEndCall()
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(FakeCallState.IDLE, viewModel.uiState.value.state)
  }
}
