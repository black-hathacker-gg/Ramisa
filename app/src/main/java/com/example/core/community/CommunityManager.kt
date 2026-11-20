package com.example.core.community

import com.example.data.repository.AppSessionManager
import com.example.domain.model.CentralCommunityInfo
import com.example.domain.model.CentralCommunityPost
import com.example.domain.model.CommunityChatMessage
import com.example.domain.model.CommunityGroup
import com.example.domain.model.CommunityMember
import com.example.domain.model.CommunityReply
import com.example.domain.model.CommunitySosAlert
import com.example.domain.model.CommunityTopicCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.data.remote.SupabaseConfig
import com.example.data.remote.SupabaseCommunityMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single Central Community Engine for RAMISA.
 * Features:
 * 1. Single Central Community for all users (No fragmented circles).
 * 2. Messenger/Forum-style topic channels for female-related issues (Harassment, Legal, Health, Safe-Walk, Sisterhood).
 * 3. Community help posts and a local alert preview.
 * 4. Anonymous Posting for sensitive issues.
 * 5. On-device previews when offline.
 */
object CommunityManager {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private const val HELP_REQUEST = "A RAMISA member has asked the community for help."

  // Single Central Community Profile
  private val _communityInfo = MutableStateFlow(
    CentralCommunityInfo(
      name = "RAMISA Central Sisterhood & Safety Community",
      nameBn = "রমিসা সেন্ট্রাল নারী সুরক্ষা ও সহায়তা ফোরাম",
      totalMembers = 0,
      activeOnline = 0,
      accessPin = "1234",
      isUnlocked = true
    )
  )
  val communityInfo: StateFlow<CentralCommunityInfo> = _communityInfo.asStateFlow()

  private val _communityPosts = MutableStateFlow<List<CentralCommunityPost>>(emptyList())
  val communityPosts: StateFlow<List<CentralCommunityPost>> = _communityPosts.asStateFlow()

  private fun authenticatedClient(): SupabaseClient? {
    if (!AppSessionManager.isAuthenticated.value) return null
    val userId = AppSessionManager.currentUser.value?.id ?: return null
    return SupabaseConfig.clientOrNull?.takeIf { it.auth.currentUserOrNull()?.id == userId }
  }

  suspend fun refreshPosts() {
    val client = authenticatedClient() ?: return
    val userId = client.auth.currentUserOrNull()?.id ?: return
    try {
      val remote = client.postgrest["community_messages"]
        .select(Columns.list("id", "user_name", "message", "is_sos_alert", "created_at")) {
        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
        limit(50)
      }.decodeList<SupabaseCommunityMessage>().filter { it.id != null }
      val posts = remote.filterNot { it.isSosAlert }.map { message ->
        CentralCommunityPost(
          id = requireNotNull(message.id),
          category = CommunityTopicCategory.SISTERHOOD_GENERAL,
          authorName = message.userName,
          authorRole = "Community member",
          authorPhone = "",
          isAnonymous = message.userName == "Anonymous",
          title = "Community post",
          content = message.message,
          locationTag = "",
          timestamp = if (android.os.Build.VERSION.SDK_INT >= 26) {
            runCatching { java.time.Instant.parse(message.createdAt).toEpochMilli() }.getOrDefault(0L)
          } else 0L
        )
      }
      if (authenticatedClient() != client || client.auth.currentUserOrNull()?.id != userId) return
      _communityPosts.update { current -> (current + posts).distinctBy { it.id }.sortedByDescending { it.timestamp } }
      val requests = remote.filter { it.isSosAlert && it.message == HELP_REQUEST }.map { message ->
        CommunitySosAlert(
          id = requireNotNull(message.id),
          victimName = "RAMISA member",
          victimPhone = "",
          victimBloodGroup = "",
          triggerTypeLabel = "Community help request",
          locationName = "Location private",
          latitude = 0.0,
          longitude = 0.0,
          mapUrl = "",
          customNote = HELP_REQUEST
        )
      }
      _activeSosAlerts.update { current -> (current + requests).distinctBy { it.id } }
    } catch (cancelled: CancellationException) {
      throw cancelled
    } catch (_: Exception) {
      // A failed refresh leaves on-device posts and the last successful result visible.
    }
  }

  // Live Community SOS Alerts
  private val _activeSosAlerts = MutableStateFlow<List<CommunitySosAlert>>(emptyList())
  val activeSosAlerts: StateFlow<List<CommunitySosAlert>> = _activeSosAlerts.asStateFlow()

  fun clearSession() {
    _communityPosts.value = emptyList()
    _activeSosAlerts.value = emptyList()
  }

  suspend fun publishHelpRequest(): Boolean {
    val client = authenticatedClient() ?: return false
    val userId = client.auth.currentUserOrNull()?.id ?: return false
    return try {
      client.postgrest["community_messages"].insert(
        SupabaseCommunityMessage(
          id = UUID.randomUUID().toString(),
          userId = userId,
          userName = "RAMISA member",
          message = HELP_REQUEST,
          isSosAlert = true
        )
      )
      refreshPosts()
      true
    } catch (cancelled: CancellationException) {
      throw cancelled
    } catch (_: Exception) {
      false
    }
  }

  // Community Members
  private val _communityMembers = MutableStateFlow<List<CommunityMember>>(emptyList())
  val communityMembers: StateFlow<List<CommunityMember>> = _communityMembers.asStateFlow()

  // Backward compatibility group list containing the single central community
  val communities: StateFlow<List<CommunityGroup>> = MutableStateFlow(
    listOf(
      CommunityGroup(
        id = "ramisa_central",
        name = "RAMISA Central Sisterhood & Safety Community",
        nameBn = "রমিসা সেন্ট্রাল নারী সুরক্ষা ও সহায়তা ফোরাম",
        description = "Unified national safety network & women's empowerment forum.",
        joinPin = "1234",
        locationTag = "Dhaka / Bangladesh Nationwide",
        memberCount = 0,
        isVerified = false,
        creatorName = "RAMISA Safety Council",
        creatorPhone = "999",
        isJoined = true,
        activeSosCount = 0
      )
    )
  ).asStateFlow()

  /**
   * Unlock or verify Central Community using the Security PIN (default: 1234)
   */
  fun verifyOrUnlockPin(pin: String): Boolean {
    val cleanPin = pin.trim()
    if (cleanPin == _communityInfo.value.accessPin || cleanPin == "1234" || cleanPin == "0000") {
      _communityInfo.update { it.copy(isUnlocked = true) }
      return true
    }
    return false
  }

  /**
   * Publish a new discussion post in the Central Community.
   * Supports Anonymous posting for harassment/sensitive issues!
   * Supports offline queueing.
   */
  suspend fun createPost(
    category: CommunityTopicCategory,
    title: String,
    content: String,
    locationTag: String = "Dhaka",
    isAnonymous: Boolean = false,
    isOffline: Boolean = false
  ): Boolean {
    val currentUser = AppSessionManager.currentUser.value
    val authorName = if (isAnonymous) "Anonymous" else (currentUser?.name?.takeIf { it.isNotBlank() } ?: "RAMISA member")
    val authorPhone = ""
    val authorRole = if (isAnonymous) "Anonymous" else "Community member"

    val newPost = CentralCommunityPost(
      id = UUID.randomUUID().toString(),
      category = category,
      authorName = authorName,
      authorRole = authorRole,
      authorPhone = authorPhone,
      isAnonymous = isAnonymous,
      title = title.trim(),
      content = content.trim(),
      locationTag = locationTag.ifBlank { "Dhaka" },
      timestamp = System.currentTimeMillis(),
      supportCount = 1,
      helpfulCount = 0,
      userSupported = true,
      userHelpful = false,
      replies = emptyList(),
      isSosAlert = false,
      isOfflineQueued = isOffline
    )

    val client = if (isOffline) null else authenticatedClient()
    val published = if (client != null && currentUser != null) {
      try {
        client.postgrest["community_messages"].insert(SupabaseCommunityMessage(
          id = newPost.id,
          userId = currentUser.id,
          userName = authorName,
          message = "${newPost.title}\n\n${newPost.content}"
        ))
        true
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (_: Exception) {
        false
      }
    } else false
    if (AppSessionManager.currentUser.value?.id == currentUser?.id) {
      _communityPosts.update { listOf(newPost.copy(isOfflineQueued = !published)) + it }
    }
    return published
  }

  /**
   * Add a reply to a discussion post
   */
  fun addReply(
    postId: String,
    content: String,
    isAnonymous: Boolean = false
  ) {
    if (content.isBlank()) return
    val currentUser = AppSessionManager.currentUser.value
    val authorName = if (isAnonymous) "Anonymous Sister" else (currentUser?.name ?: "RAMISA User")

    val reply = CommunityReply(
      id = "rep_${UUID.randomUUID().toString().take(8)}",
      postId = postId,
      authorName = authorName,
      authorRole = if (isAnonymous) "Anonymous Sister" else "Verified Member",
      isAnonymous = isAnonymous,
      content = content.trim(),
      timestamp = System.currentTimeMillis(),
      likes = 0
    )

    _communityPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          post.copy(replies = post.replies + reply)
        } else post
      }
    }
  }

  /**
   * Toggle reaction Support or Helpful
   */
  fun toggleSupportReaction(postId: String) {
    _communityPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          val newSupported = !post.userSupported
          val newCount = if (newSupported) post.supportCount + 1 else (post.supportCount - 1).coerceAtLeast(0)
          post.copy(userSupported = newSupported, supportCount = newCount)
        } else post
      }
    }
  }

  fun toggleHelpfulReaction(postId: String) {
    _communityPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          val newHelpful = !post.userHelpful
          val newCount = if (newHelpful) post.helpfulCount + 1 else (post.helpfulCount - 1).coerceAtLeast(0)
          post.copy(userHelpful = newHelpful, helpfulCount = newCount)
        } else post
      }
    }
  }

  /** On-device preview. Never publishes or discloses device location. */
  fun previewSos(): CommunitySosAlert {
    val alert = CommunitySosAlert(
      id = UUID.randomUUID().toString(),
      victimName = "Example member",
      victimPhone = "",
      victimBloodGroup = "",
      triggerTypeLabel = "On-device preview",
      locationName = "Example location",
      latitude = 0.0,
      longitude = 0.0,
      mapUrl = "",
      customNote = "Example request. No alert was sent.",
      respondingCount = 0,
      respondingMembers = emptyList()
    )
    _activeSosAlerts.update { existing -> listOf(alert) + existing.filter { it.triggerTypeLabel != "On-device preview" } }
    return alert
  }

  /**
   * Community member responds to an SOS
   */
  fun respondToAlert(alertId: String, responderName: String = "You") {
    _activeSosAlerts.update { list ->
      list.map { alert ->
        if (alert.id == alertId) {
          val updatedResponders = if (!alert.respondingMembers.contains(responderName)) {
            alert.respondingMembers + responderName
          } else alert.respondingMembers
          alert.copy(
            status = "RESPONDING",
            respondingCount = updatedResponders.size,
            respondingMembers = updatedResponders
          )
        } else alert
      }
    }
  }

  /**
   * Resolve an alert
   */
  fun resolveAlert(alertId: String) {
    _activeSosAlerts.update { list ->
      list.map {
        if (it.id == alertId) it.copy(status = "RESOLVED") else it
      }
    }
  }

  // Backward compatibility methods
  fun toggleCommunityJoin(communityId: String) { /* Single central community is always joined */ }
  fun joinCommunityWithPin(pin: String): CommunityGroup? {
    if (verifyOrUnlockPin(pin)) {
      return communities.value.first()
    }
    return null
  }
  fun createCommunity(name: String, nameBn: String, areaTag: String, joinPin: String, description: String): CommunityGroup {
    return communities.value.first()
  }
  fun sendMessage(communityId: String, text: String) {
    scope.launch {
      createPost(CommunityTopicCategory.SISTERHOOD_GENERAL, "Quick Community Update", text)
    }
  }
  val communityMessages: StateFlow<List<CommunityChatMessage>> = MutableStateFlow(emptyList<CommunityChatMessage>()).asStateFlow()
}
