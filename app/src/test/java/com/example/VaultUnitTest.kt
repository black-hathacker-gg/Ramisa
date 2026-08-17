package com.example

import com.example.data.repository.VaultRepositoryImpl
import com.example.domain.model.EvidenceType
import com.example.features.vault.VaultViewModel
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
class VaultUnitTest {

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
  fun testVault_UnlockAndLock() = runTest(testDispatcher) {
    val repository = VaultRepositoryImpl()
    val viewModel = VaultViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isUnlocked)

    // Test with wrong PIN
    viewModel.onPinChange("9999")
    viewModel.unlockVault()
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isUnlocked)

    // Test with correct PIN (1234)
    viewModel.onPinChange("1234")
    viewModel.unlockVault()
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isUnlocked)

    // Lock vault again
    viewModel.lockVault()
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isUnlocked)
  }

  @Test
  fun testVault_AddNoteAndAudioEvidence() = runTest(testDispatcher) {
    val repository = VaultRepositoryImpl()
    val viewModel = VaultViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Unlock
    viewModel.onPinChange("1234")
    viewModel.unlockVault()
    testDispatcher.scheduler.advanceUntilIdle()

    val initialCount = viewModel.uiState.value.evidenceList.size

    // Add note
    viewModel.addIncidentNote("Test Incident", "Observation on Mirpur road")
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(initialCount + 1, viewModel.uiState.value.evidenceList.size)
    val firstItem = viewModel.uiState.value.evidenceList.first()
    assertEquals("Test Incident", firstItem.title)
    assertEquals(EvidenceType.INCIDENT_NOTE, firstItem.type)

    // Delete note
    viewModel.deleteEvidence(firstItem.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(initialCount, viewModel.uiState.value.evidenceList.size)
  }
}
