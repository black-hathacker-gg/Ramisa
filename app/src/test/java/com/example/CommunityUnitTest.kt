package com.example

import com.example.features.community.CommunityViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityUnitTest {

  @Test
  fun testCommunity_LiveSharingAndFilters() {
    val viewModel = CommunityViewModel()

    assertFalse(viewModel.uiState.value.isLiveSharingEnabled)
    assertTrue(viewModel.uiState.value.areaReports.isNotEmpty())

    // Start Live Share
    viewModel.startLiveSharing(45)
    assertTrue(viewModel.uiState.value.isLiveSharingEnabled)
    assertNotNull(viewModel.uiState.value.activeLiveSession)
    assertTrue(viewModel.uiState.value.activeLiveSession!!.shareableWebUrl.contains("ramisa.live/track/"))

    // Search and Filter
    viewModel.updateSearchQuery("Curzon")
    assertEquals("Curzon", viewModel.uiState.value.searchQuery)

    viewModel.setFilter("SAFE")
    assertEquals("SAFE", viewModel.uiState.value.selectedFilter)

    // Stop Live Share
    viewModel.stopLiveSharing()
    assertFalse(viewModel.uiState.value.isLiveSharingEnabled)
  }
}
