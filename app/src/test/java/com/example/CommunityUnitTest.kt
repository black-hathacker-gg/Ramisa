package com.example

import com.example.core.community.CommunityManager
import com.example.domain.model.CommunityTopicCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityUnitTest {
  @Test
  fun offlinePostStaysLocal() = runTest {
    val published = CommunityManager.createPost(
      category = CommunityTopicCategory.SISTERHOOD_GENERAL,
      title = "Travel check-in",
      content = "Example discussion",
      isAnonymous = true,
      isOffline = true
    )
    assertFalse(published)
    val post = CommunityManager.communityPosts.value.first()
    assertEquals("Anonymous", post.authorName)
    assertEquals("", post.authorPhone)
    assertTrue(post.isOfflineQueued)
  }

  @Test
  fun previewContainsNoContactOrLocationLink() {
    val alert = CommunityManager.previewSos()
    assertEquals("", alert.victimPhone)
    assertEquals("", alert.mapUrl)
    assertEquals(0, alert.respondingCount)
    assertEquals(alert, CommunityManager.activeSosAlerts.value.single())
  }
}
