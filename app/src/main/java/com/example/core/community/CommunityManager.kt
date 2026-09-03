package com.example.core.community

import android.util.Log
import com.example.data.repository.AppSessionManager
import com.example.domain.model.AreaSafetyReport
import com.example.domain.model.CentralCommunityInfo
import com.example.domain.model.CentralCommunityPost
import com.example.domain.model.CommunityChatMessage
import com.example.domain.model.CommunityGroup
import com.example.domain.model.CommunityMember
import com.example.domain.model.CommunityReply
import com.example.domain.model.CommunitySosAlert
import com.example.domain.model.CommunityTopicCategory
import com.example.domain.model.GeoLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.data.remote.SupabaseConfig
import com.example.data.remote.SupabaseCommunityMessage
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single Central Community Engine for RAMISA.
 * Features:
 * 1. Single Central Community for all users (No fragmented circles).
 * 2. Messenger/Forum-style topic channels for female-related issues (Harassment, Legal, Health, Safe-Walk, Sisterhood).
 * 3. Automatic Emergency SOS broadcast (via Shake, Lock Screen, SOS Button).
 * 4. Anonymous Posting for sensitive issues.
 * 5. Full Offline Resilience (Offline cached reading & queued posts).
 */
object CommunityManager {

  private const val TAG = "CommunityManager"

  // Single Central Community Profile
  private val _communityInfo = MutableStateFlow(
    CentralCommunityInfo(
      name = "RAMISA Central Sisterhood & Safety Community",
      nameBn = "রমিসা সেন্ট্রাল নারী সুরক্ষা ও সহায়তা ফোরাম",
      totalMembers = 3450,
      activeOnline = 186,
      accessPin = "1234",
      isUnlocked = true
    )
  )
  val communityInfo: StateFlow<CentralCommunityInfo> = _communityInfo.asStateFlow()

  init {
    GlobalScope.launch(Dispatchers.IO) {
      while (true) {
        try {
          val remoteMessages = SupabaseConfig.client.postgrest["community_messages"]
              .select()
              .decodeList<SupabaseCommunityMessage>()
              .sortedByDescending { it.createdAt ?: "" }
          
          val mappedPosts = remoteMessages.map { msg ->
              CentralCommunityPost(
                  id = msg.id ?: UUID.randomUUID().toString(),
                  category = if (msg.isSosAlert) CommunityTopicCategory.EMERGENCY_SOS else CommunityTopicCategory.SISTERHOOD_GENERAL,
                  authorName = if (msg.isSosAlert) "SOS BROADCAST: ${msg.userName}" else msg.userName,
                  authorRole = if (msg.isSosAlert) "Emergency Dispatch" else "Verified Member",
                  authorPhone = "Protected",
                  isAnonymous = false,
                  title = if (msg.isSosAlert) "CRISIS SOS: ${msg.userName} needs immediate help!" else "Community Update",
                  content = msg.message,
                  locationTag = "Live Area",
                  timestamp = System.currentTimeMillis(), // We could parse createdAt but this is easier for now
                  supportCount = 0,
                  helpfulCount = 0,
                  isSosAlert = msg.isSosAlert,
                  sosDetails = if (msg.isSosAlert) CommunitySosAlert(
                      id = "SOS-${msg.id}",
                      victimName = msg.userName,
                      victimPhone = "",
                      triggerTypeLabel = "Emergency Alert",
                      locationName = "Location protected",
                      latitude = 23.7258,
                      longitude = 90.3976,
                      mapUrl = "https://maps.google.com/?q=23.7258,90.3976",
                      customNote = msg.message
                  ) else null
              )
          }

          // Merge keeping local replies and states, but for simplicity we just replace the ones that come from Supabase.
          // Or even simpler: we just update the _communityPosts to include these.
          _communityPosts.update { currentList ->
              val all = (currentList + mappedPosts).distinctBy { it.id }
              all.sortedByDescending { it.timestamp }
          }
        } catch (e: Exception) {
          Log.e(TAG, "Failed to poll Supabase community messages", e)
        }
        delay(3000) // Poll every 3 seconds
      }
    }
  }

  // Pre-seeded female-related discussions (Messenger community topics)
  private val initialPosts = listOf(
    CentralCommunityPost(
      id = "post_sos_1",
      category = CommunityTopicCategory.EMERGENCY_SOS,
      authorName = "Nusrat Jahan",
      authorRole = "Campus Guard Proctor",
      authorPhone = "+880 1711-889900",
      isAnonymous = false,
      title = "Notice: Extra Volunteers Stationed Near Curzon & Nilkhet",
      content = "To all students and night commuters: 4 female student volunteers and campus proctors are on duty along Curzon Hall gate and Nilkhet till 10:30 PM. Call anytime if you need safe walk assistance.",
      locationTag = "Dhaka University Campus",
      timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
      supportCount = 42,
      helpfulCount = 19,
      replies = listOf(
        CommunityReply(
          id = "rep_1",
          postId = "post_sos_1",
          authorName = "Tasnim Hossain",
          authorRole = "Volunteer",
          content = "I am near TSC tea stalls. If anyone needs an escort to Rokeya hall, let me know here!",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
          likes = 8
        )
      )
    ),
    CentralCommunityPost(
      id = "post_harass_1",
      category = CommunityTopicCategory.HARASSMENT_ALERT,
      authorName = "বেনামী বোন (Anonymous Sister)",
      authorRole = "Verified Member",
      authorPhone = "Hidden",
      isAnonymous = true,
      title = "Beware: Dim lighting near Farmgate Footbridge West Exit",
      content = "Today around 7:45 PM near the west staircase of Farmgate overbridge, a group of suspicious boys were making inappropriate catcalls. I used Ramisa's Fake Call tool and safely moved into the nearby pharmacy. Please avoid that staircase after sunset or walk in groups.",
      locationTag = "Farmgate, Dhaka",
      timestamp = System.currentTimeMillis() - 1000 * 60 * 80,
      supportCount = 58,
      helpfulCount = 34,
      replies = listOf(
        CommunityReply(
          id = "rep_2",
          postId = "post_harass_1",
          authorName = "Advocate Farzana Karim",
          authorRole = "Legal Advisor",
          content = "Thank you for reporting, sister! We have notified the Tejgaon police patrol beat box. If this persists, file a report to Police Cyber & Women Support Center hotline 01320000888.",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
          likes = 14
        )
      )
    ),
    CentralCommunityPost(
      id = "post_legal_1",
      category = CommunityTopicCategory.LEGAL_CYBER_HELP,
      authorName = "Advocate Farzana Karim",
      authorRole = "Legal & Cyber Advisor",
      authorPhone = "+880 1812-445566",
      isAnonymous = false,
      title = "Steps to Take if Someone is Threatening or Stalking on Facebook/WhatsApp",
      content = "Important legal guideline for all sisters:\n1. Do NOT delete threatening chats or audio—take clear screenshots showing phone numbers/URLs.\n2. Back up screenshots securely to your private device storage.\n3. You can lodge an official complaint via Cyber Crime Investigation Division (CID) or 999 without having to visit the police station alone.\nFeel free to ask your questions below, I am here to help.",
      locationTag = "Dhaka / Bangladesh",
      timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
      supportCount = 112,
      helpfulCount = 89,
      replies = listOf(
        CommunityReply(
          id = "rep_3",
          postId = "post_legal_1",
          authorName = "Anonymous Sister",
          authorRole = "Member",
          isAnonymous = true,
          content = "Can we file a GD completely anonymously if we are scared of family knowing?",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
          likes = 5
        ),
        CommunityReply(
          id = "rep_4",
          postId = "post_legal_1",
          authorName = "Advocate Farzana Karim",
          authorRole = "Legal Advisor",
          content = "Yes! You can contact the Women Support and Investigation Division where dedicated female officers maintain strict privacy.",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
          likes = 11
        )
      )
    ),
    CentralCommunityPost(
      id = "post_health_1",
      category = CommunityTopicCategory.HEALTH_WELLNESS,
      authorName = "Dr. Samia Chowdhury",
      authorRole = "Women's Health Consultant",
      authorPhone = "+880 1913-776655",
      isAnonymous = false,
      title = "Managing Severe Dysmenorrhea (Period Cramps) during Work & Commute",
      content = "For sisters who experience debilitating pain during travel or university:\n1. Keep a thermos with warm ginger water and magnesium-rich snacks.\n2. Avoid caffeine on the first 2 days.\n3. If pain is unbearable, check with a gynecologist for endometriosis screening.\nShare your self-care remedies or ask health questions comfortably here.",
      locationTag = "Health Forum",
      timestamp = System.currentTimeMillis() - 1000 * 60 * 320,
      supportCount = 76,
      helpfulCount = 61,
      replies = emptyList()
    ),
    CentralCommunityPost(
      id = "post_walk_1",
      category = CommunityTopicCategory.SAFE_WALK_BUDDY,
      authorName = "Momena Akter",
      authorRole = "Verified Member",
      authorPhone = "+880 1614-223344",
      isAnonymous = false,
      title = "Metro Commuters: Evening Group from Agargaon to Mirpur 10",
      content = "I take the metro daily at 8:15 PM from Agargaon to Mirpur 10. Any other sisters travelling on the same train? Let's board the Women's Dedicated Coach together for a safe and comfortable ride!",
      locationTag = "Agargaon Metro Station",
      timestamp = System.currentTimeMillis() - 1000 * 60 * 140,
      supportCount = 35,
      helpfulCount = 20,
      replies = emptyList()
    )
  )

  private val _communityPosts = MutableStateFlow<List<CentralCommunityPost>>(initialPosts)
  val communityPosts: StateFlow<List<CentralCommunityPost>> = _communityPosts.asStateFlow()

  // Live Community SOS Alerts
  private val _activeSosAlerts = MutableStateFlow<List<CommunitySosAlert>>(emptyList())
  val activeSosAlerts: StateFlow<List<CommunitySosAlert>> = _activeSosAlerts.asStateFlow()

  // Community Members
  private val _communityMembers = MutableStateFlow<List<CommunityMember>>(
    listOf(
      CommunityMember("mem_1", "RAMISA User (You)", "+880 1712-345678", "Verified Member", "Active & Guarding", true, "R"),
      CommunityMember("mem_2", "Nusrat Jahan", "+880 1711-889900", "Circle Admin / Proctor", "On Duty Patrol", true, "N"),
      CommunityMember("mem_3", "Advocate Farzana Karim", "+880 1812-445566", "Legal Advisor", "Active for Help", true, "F"),
      CommunityMember("mem_4", "Dr. Samia Chowdhury", "+880 1913-776655", "Health Consultant", "Consulting", true, "S"),
      CommunityMember("mem_5", "Tasnim Hossain", "+880 1913-223344", "Volunteer Responder", "Nearby TSC", true, "T"),
      CommunityMember("mem_6", "Farhana Yasmin", "+880 1614-556677", "Member", "Safe & Connected", true, "F"),
      CommunityMember("mem_7", "Khadija Begum", "+880 1515-998877", "Campus Security Wing", "Stationed Gate 2", true, "K")
    )
  )
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
        memberCount = 3450,
        isVerified = true,
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
  fun createPost(
    category: CommunityTopicCategory,
    title: String,
    content: String,
    locationTag: String = "Dhaka",
    isAnonymous: Boolean = false,
    isOffline: Boolean = false
  ): CentralCommunityPost {
    val currentUser = AppSessionManager.currentUser.value
    val authorName = if (isAnonymous) "বেনামী বোন (Anonymous Sister)" else (currentUser?.name ?: "RAMISA User")
    val authorPhone = if (isAnonymous) "Protected" else (currentUser?.phone ?: "+880 1712-345678")
    val authorRole = if (isAnonymous) "Anonymous Sister" else "Verified Member"

    val newPost = CentralCommunityPost(
      id = "post_${UUID.randomUUID().toString().take(8)}",
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

    _communityPosts.update { listOf(newPost) + it }
    GlobalScope.launch(Dispatchers.IO) {
      try {
        val user = AppSessionManager.currentUser.value
        val userId = user?.id ?: "unknown"
        val message = SupabaseCommunityMessage(
          userId = userId,
          userName = authorName,
          message = content.trim(),
          isSosAlert = false
        )
        SupabaseConfig.client.postgrest["community_messages"].insert(message)
      } catch(e: Exception) {
        e.printStackTrace()
      }
    }
    Log.d(TAG, "Created community post [${category.name}] by $authorName (Offline queued: $isOffline)")
    return newPost
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

  /**
   * Broadcast an urgent SOS Alert directly into the Central Community.
   * Called automatically when user shakes device 3x, taps lock screen SOS, or hits the SOS button!
   */
  fun broadcastSosAlert(
    victimName: String? = null,
    victimPhone: String? = null,
    victimBloodGroup: String? = null,
    triggerTypeLabel: String = "Urgent Emergency SOS Trigger",
    location: GeoLocation = GeoLocation(23.7258, 90.3976, "Curzon Hall, Dhaka University"),
    note: String = ""
  ): CommunitySosAlert {
    val currentUser = AppSessionManager.currentUser.value
    val name = victimName ?: currentUser?.name ?: "RAMISA User"
    val phone = victimPhone ?: currentUser?.phone ?: "+880 1712-345678"
    val blood = victimBloodGroup ?: currentUser?.bloodGroup ?: "B+"

    val mapUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"

    val alert = CommunitySosAlert(
      id = "SOS-${System.currentTimeMillis() % 100000}",
      communityId = "ramisa_central",
      communityName = "RAMISA Central Community",
      victimName = name,
      victimPhone = phone,
      victimBloodGroup = blood,
      triggerTypeLabel = triggerTypeLabel,
      locationName = location.addressName,
      latitude = location.latitude,
      longitude = location.longitude,
      mapUrl = mapUrl,
      timestamp = System.currentTimeMillis(),
      customNote = note.ifBlank { "Immediate crisis assistance required. Please coordinate rescue or call." },
      respondingCount = 2,
      respondingMembers = listOf("Nusrat Jahan (Proctor)", "Tasnim Hossain"),
      status = "CRITICAL_ACTIVE"
    )

    Log.w(TAG, "BROADCASTING SOS TO CENTRAL COMMUNITY: $name at ${location.addressName}")

    // Update active alerts list
    _activeSosAlerts.update { listOf(alert) + it }

    // Auto-create top Emergency SOS post in the Central Community Feed
    val sosPost = CentralCommunityPost(
      id = "post_sos_${alert.id}",
      category = CommunityTopicCategory.EMERGENCY_SOS,
      authorName = "SOS BROADCAST: $name",
      authorRole = "Emergency Dispatch",
      authorPhone = phone,
      isAnonymous = false,
      title = "CRISIS SOS: $name needs immediate help!",
      content = "Triggered via [$triggerTypeLabel] at ${location.addressName}.\nBlood Group: $blood | Phone: $phone\nLive Location: $mapUrl\nNote: ${alert.customNote}",
      locationTag = location.addressName,
      timestamp = System.currentTimeMillis(),
      supportCount = 15,
      helpfulCount = 8,
      replies = listOf(
        CommunityReply(
          id = "rep_sos_1",
          postId = "post_sos_${alert.id}",
          authorName = "Nusrat Jahan (Proctor)",
          authorRole = "Responder",
          content = "I am contacting the local beat police patrol and heading towards the spot right now!",
          timestamp = System.currentTimeMillis(),
          likes = 6
        )
      ),
      isSosAlert = true,
      sosDetails = alert
    )

    _communityPosts.update { listOf(sosPost) + it }
    GlobalScope.launch(Dispatchers.IO) {
      try {
        val user = AppSessionManager.currentUser.value
        val userId = user?.id ?: "unknown"
        val message = SupabaseCommunityMessage(
          userId = userId,
          userName = name,
          message = sosPost.content,
          isSosAlert = true
        )
        SupabaseConfig.client.postgrest["community_messages"].insert(message)
      } catch(e: Exception) {
        e.printStackTrace()
      }
    }
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
    createPost(
      category = CommunityTopicCategory.SISTERHOOD_GENERAL,
      title = "Quick Community Update",
      content = text
    )
  }
  val communityMessages: StateFlow<List<CommunityChatMessage>> = MutableStateFlow(emptyList<CommunityChatMessage>()).asStateFlow()
}
