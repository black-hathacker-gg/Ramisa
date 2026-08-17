package com.example

import com.example.data.repository.EmergencyContactRepositoryImpl
import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.TriggerType
import com.example.features.contacts.ContactsViewModel
import com.example.features.emergency.EmergencyViewModel
import com.example.features.history.HistoryViewModel
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
class EmergencyAndContactsUnitTest {

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
  fun testContactsViewModel_AddAndToggle() = runTest(testDispatcher) {
    val repository = EmergencyContactRepositoryImpl()
    val viewModel = ContactsViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    val initialCount = viewModel.uiState.value.contacts.size

    // Add a new 4th contact
    viewModel.addContact("Ayesha Siddiqua (Aunt)", "+880 1612-998877", "Aunt")
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(initialCount + 1, viewModel.uiState.value.contacts.size)
    val added = viewModel.uiState.value.contacts.last()
    assertEquals("Ayesha Siddiqua (Aunt)", added.name)
    assertEquals(initialCount + 1, added.priority)

    // Toggle SMS
    viewModel.toggleSms(added.id)
    testDispatcher.scheduler.advanceUntilIdle()
    val toggled = viewModel.uiState.value.contacts.first { it.id == added.id }
    assertFalse(toggled.smsEnabled)

    // Delete contact
    viewModel.deleteContact(added.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(initialCount, viewModel.uiState.value.contacts.size)
  }

  @Test
  fun testEmergencyViewModel_TriggerAndResolve() = runTest(testDispatcher) {
    val repository = EmergencyRepositoryImpl()
    val viewModel = EmergencyViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger SOS
    viewModel.triggerSos(TriggerType.SOS_BUTTON)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.emergencyEventId.startsWith("EMG-"))

    // Incorrect PIN resolution fails
    viewModel.onPinEntered("9999")
    var resolved = false
    viewModel.resolveEmergency { resolved = true }
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(resolved)
    assertNotNull(viewModel.uiState.value.pinErrorMessage)

    // Correct PIN resolution succeeds
    viewModel.onPinEntered("1234")
    viewModel.resolveEmergency { resolved = true }
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(resolved)
  }

  @Test
  fun testHistoryViewModel_LoadsHistoryAndClears() = runTest(testDispatcher) {
    val repository = EmergencyRepositoryImpl()
    val viewModel = HistoryViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.events.isNotEmpty())

    viewModel.clearHistory()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.events.isEmpty())
  }
}
