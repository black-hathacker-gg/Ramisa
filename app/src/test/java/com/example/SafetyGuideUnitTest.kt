package com.example

import com.example.data.repository.SafetyGuideRepository
import com.example.domain.model.TipCategory
import com.example.features.guide.SafetyGuideViewModel
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
class SafetyGuideUnitTest {

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
  fun testSafetyGuide_LoadAndCategoryFilter() = runTest(testDispatcher) {
    val repository = SafetyGuideRepository()
    val viewModel = SafetyGuideViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.tips.isNotEmpty())

    // Filter by Legal Rights
    viewModel.selectCategory(TipCategory.LEGAL_RIGHTS)
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.tips.all { it.category == TipCategory.LEGAL_RIGHTS })

    // Expand tip
    val tipId = viewModel.uiState.value.tips.first().id
    viewModel.toggleExpand(tipId)
    assertEquals(tipId, viewModel.uiState.value.expandedTipId)

    // Collapse tip
    viewModel.toggleExpand(tipId)
    assertEquals(null, viewModel.uiState.value.expandedTipId)
  }
}
