package com.example.features.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.community.CommunityManager
import com.example.data.repository.AppSessionManager
import com.example.domain.model.AreaSafetyReport
import com.example.domain.model.CentralCommunityInfo
import com.example.domain.model.CentralCommunityPost
import com.example.domain.model.CommunityMember
import com.example.domain.model.CommunitySosAlert
import com.example.domain.model.CommunityTopicCategory
import com.example.domain.model.GeoLocation
import com.example.domain.model.LiveShareSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CommunityUiState(
  val selectedTab: Int = 0, // 0: Discussions & Messenger Forum, 1: Emergency SOS Feed, 2: Active Sisters & Advisors, 3: Safe Zones
  val selectedCategory: CommunityTopicCategory = CommunityTopicCategory.ALL,
  val searchQuery: String = "",
  val isOfflineMode: Boolean = false,
  
  // Post Creation Dialog
  val isCreatePostDialogVisible: Boolean = false,
  val newPostCategory: CommunityTopicCategory = CommunityTopicCategory.SISTERHOOD_GENERAL,
  val newPostTitle: String = "",
  val newPostContent: String = "",
  val newPostLocation: String = "Dhaka",
  val isNewPostAnonymous: Boolean = false,
  
  // Reply State
  val selectedPostForReply: CentralCommunityPost? = null,
  val replyInputText: String = "",
  val isReplyAnonymous: Boolean = false,

  // PIN Unlock
  val pinInput: String = "",
  val pinError: String? = null,
  val pinSuccessMessage: String? = null,

  // Safe Zones
  val areaReports: List<AreaSafetyReport> = emptyList(),
  val selectedSafeZoneFilter: String = "ALL",
  val isReportingDialogVisible: Boolean = false,

  val testSosSuccessMessage: String? = null
)

class CommunityViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CommunityUiState())
  val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

  val communityInfo: StateFlow<CentralCommunityInfo> = CommunityManager.communityInfo
  val communityPosts: StateFlow<List<CentralCommunityPost>> = CommunityManager.communityPosts
  val activeSosAlerts: StateFlow<List<CommunitySosAlert>> = CommunityManager.activeSosAlerts
  val communityMembers: StateFlow<List<CommunityMember>> = CommunityManager.communityMembers

  // Backward compatibility properties
  val communities = CommunityManager.communities
  val communityMessages = CommunityManager.communityMessages

  init {
    loadDefaultAreaReports()
  }

  fun setSelectedTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index) }
  }

  fun setSelectedCategory(category: CommunityTopicCategory) {
    _uiState.update { it.copy(selectedCategory = category) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun toggleOfflineMode() {
    _uiState.update { it.copy(isOfflineMode = !it.isOfflineMode) }
  }

  // --- Create Post Dialog Management ---
  fun showCreatePostDialog(show: Boolean) {
    _uiState.update {
      it.copy(
        isCreatePostDialogVisible = show,
        newPostTitle = "",
        newPostContent = "",
        newPostLocation = "Dhaka",
        isNewPostAnonymous = false
      )
    }
  }

  fun updateNewPostCategory(category: CommunityTopicCategory) {
    _uiState.update { it.copy(newPostCategory = category) }
  }

  fun updateNewPostTitle(title: String) {
    _uiState.update { it.copy(newPostTitle = title) }
  }

  fun updateNewPostContent(content: String) {
    _uiState.update { it.copy(newPostContent = content) }
  }

  fun updateNewPostLocation(loc: String) {
    _uiState.update { it.copy(newPostLocation = loc) }
  }

  fun toggleNewPostAnonymous(anonymous: Boolean) {
    _uiState.update { it.copy(isNewPostAnonymous = anonymous) }
  }

  fun submitNewPost() {
    val title = _uiState.value.newPostTitle.trim()
    val content = _uiState.value.newPostContent.trim()
    if (title.isBlank() || content.isBlank()) return

    CommunityManager.createPost(
      category = _uiState.value.newPostCategory,
      title = title,
      content = content,
      locationTag = _uiState.value.newPostLocation,
      isAnonymous = _uiState.value.isNewPostAnonymous,
      isOffline = _uiState.value.isOfflineMode
    )

    _uiState.update {
      it.copy(
        isCreatePostDialogVisible = false,
        testSosSuccessMessage = if (it.isOfflineMode)
          "Post saved to Offline Cache & will sync automatically!"
        else
          "Discussion posted to RAMISA Central Community!"
      )
    }
  }

  // --- Reactions ---
  fun toggleSupport(postId: String) {
    CommunityManager.toggleSupportReaction(postId)
  }

  fun toggleHelpful(postId: String) {
    CommunityManager.toggleHelpfulReaction(postId)
  }

  // --- Reply Management ---
  fun openReplyDialog(post: CentralCommunityPost) {
    _uiState.update {
      it.copy(
        selectedPostForReply = post,
        replyInputText = "",
        isReplyAnonymous = false
      )
    }
  }

  fun closeReplyDialog() {
    _uiState.update { it.copy(selectedPostForReply = null, replyInputText = "") }
  }

  fun updateReplyInput(text: String) {
    _uiState.update { it.copy(replyInputText = text) }
  }

  fun toggleReplyAnonymous(anonymous: Boolean) {
    _uiState.update { it.copy(isReplyAnonymous = anonymous) }
  }

  fun submitReply() {
    val post = _uiState.value.selectedPostForReply ?: return
    val text = _uiState.value.replyInputText.trim()
    if (text.isBlank()) return

    CommunityManager.addReply(
      postId = post.id,
      content = text,
      isAnonymous = _uiState.value.isReplyAnonymous
    )

    _uiState.update { it.copy(selectedPostForReply = null, replyInputText = "") }
  }

  // --- SOS Management ---
  fun broadcastManualSosToCommunity(triggerType: String = "Manual Central SOS Trigger") {
    val alert = CommunityManager.broadcastSosAlert(
      triggerTypeLabel = triggerType,
      location = GeoLocation(23.7258, 90.3976, "Curzon Hall, Dhaka University"),
      note = "URGENT: I need emergency help and escort assistance!"
    )
    _uiState.update {
      it.copy(
        testSosSuccessMessage = "Emergency SOS Broadcast sent to RAMISA Central Community! Alert ID: ${alert.id}"
      )
    }
  }

  fun respondToSosAlert(alertId: String) {
    val currentUserName = AppSessionManager.currentUser.value?.name ?: "RAMISA User"
    CommunityManager.respondToAlert(alertId, "$currentUserName (You)")
  }

  fun resolveSosAlert(alertId: String) {
    CommunityManager.resolveAlert(alertId)
  }

  fun clearTestSosMessage() {
    _uiState.update { it.copy(testSosSuccessMessage = null) }
  }

  // --- PIN unlock ---
  fun updatePinInput(pin: String) {
    _uiState.update { it.copy(pinInput = pin, pinError = null, pinSuccessMessage = null) }
  }

  fun verifyPin() {
    val pin = _uiState.value.pinInput.trim()
    if (CommunityManager.verifyOrUnlockPin(pin)) {
      _uiState.update { it.copy(pinSuccessMessage = "Verified Central Sisterhood Member", pinError = null) }
    } else {
      _uiState.update { it.copy(pinError = "ভুল পিন নম্বর! (Try default 1234)") }
    }
  }

  // --- Safe Zone Reports ---
  private fun loadDefaultAreaReports() {
    val defaultReports = listOf(
      AreaSafetyReport(
        id = "dhk_du",
        areaName = "Dhaka University Campus (Curzon & TSC)",
        areaNameBn = "ঢাকা বিশ্ববিদ্যালয় এলাকা (কার্জন ও টিএসসি)",
        safetyScore = 91,
        lightingRating = 4.7f,
        policePatrolRating = 4.8f,
        crowdDensity = "High",
        crowdDensityBn = "উচ্চ ভিড়",
        totalReviews = 342,
        lastReportedMinutesAgo = 5,
        isCautionZone = false,
        safetyTips = listOf("Campus security active 24/7", "Well lit paths around Rokeya & Shamsun Nahar halls"),
        safetyTipsBn = listOf("২৪ ঘণ্টা ক্যাম্পাস সিকিউরিটি সক্রিয়", "রোকেয়া ও শামসুন্নাহার হলের আশপাশের রাস্তা ভালোভাবে আলোকিত")
      ),
      AreaSafetyReport(
        id = "dhk_dhanmondi",
        areaName = "Dhanmondi Lake & Satmasjid Road",
        areaNameBn = "ধানমন্ডি লেক ও সাতমসজিদ রোড",
        safetyScore = 86,
        lightingRating = 4.5f,
        policePatrolRating = 4.3f,
        crowdDensity = "High",
        crowdDensityBn = "উচ্চ ভিড়",
        totalReviews = 512,
        lastReportedMinutesAgo = 12,
        isCautionZone = false,
        safetyTips = listOf("Rabindra Sarobar has heavy police checkposts", "Main road remains active till 11 PM"),
        safetyTipsBn = listOf("রবীন্দ্র সরোবরের কাছে পুলিশি চেকপোস্ট রয়েছে", "মূল সড়ক রাত ১১টা পর্যন্ত সচল থাকে")
      ),
      AreaSafetyReport(
        id = "dhk_farmgate",
        areaName = "Farmgate Foot Overbridge & Park Road",
        areaNameBn = "ফার্মগেট ফুটওভারব্রিজ ও পার্ক রোড",
        safetyScore = 58,
        lightingRating = 2.8f,
        policePatrolRating = 3.2f,
        crowdDensity = "Moderate",
        crowdDensityBn = "মাঝারি ভিড়",
        totalReviews = 410,
        lastReportedMinutesAgo = 8,
        isCautionZone = true,
        safetyTips = listOf("Avoid dim underpass late night", "Keep belongings secure near bus counters"),
        safetyTipsBn = listOf("দেরি রাতে অন্ধকার আন্ডারপাস এড়িয়ে চলুন", "বাস কাউন্টারের পাশে ব্যাগ সাবধানে রাখুন")
      ),
      AreaSafetyReport(
        id = "dhk_gulshan",
        areaName = "Gulshan 1 & 2 Diplomatic Zone",
        areaNameBn = "গুলশান ১ ও ২ কূটনৈতিক এলাকা",
        safetyScore = 95,
        lightingRating = 4.9f,
        policePatrolRating = 5.0f,
        crowdDensity = "Moderate",
        crowdDensityBn = "মাঝারি ভিড়",
        totalReviews = 278,
        lastReportedMinutesAgo = 3,
        isCautionZone = false,
        safetyTips = listOf("Diplomatic police patrol active 24/7", "CCTV surveillance network covered"),
        safetyTipsBn = listOf("কূটনৈতিক পুলিশ সার্বক্ষণিক টহল দেয়", "পুরো এলাকা সিসিটিভি ক্যামেরার আওতাভুক্ত")
      )
    )

    _uiState.update { it.copy(areaReports = defaultReports) }
  }

  fun setSafeZoneFilter(filter: String) {
    _uiState.update { it.copy(selectedSafeZoneFilter = filter) }
  }

  fun showReportingDialog(show: Boolean) {
    _uiState.update { it.copy(isReportingDialogVisible = show) }
  }

  fun submitCommunityReport(
    areaName: String,
    areaNameBn: String,
    safetyScore: Int,
    lightingRating: Float,
    isCaution: Boolean,
    note: String
  ) {
    val newReport = AreaSafetyReport(
      id = "rep_${UUID.randomUUID().toString().take(6)}",
      areaName = areaName.ifBlank { "Reported Area" },
      areaNameBn = areaNameBn.ifBlank { areaName },
      district = "Dhaka",
      safetyScore = safetyScore,
      lightingRating = lightingRating,
      policePatrolRating = if (isCaution) 2.5f else 4.5f,
      crowdDensity = if (isCaution) "Isolated" else "Moderate",
      crowdDensityBn = if (isCaution) "নির্জন" else "মাঝারি ভিড়",
      totalReviews = 1,
      lastReportedMinutesAgo = 1,
      isCautionZone = isCaution,
      safetyTips = if (note.isNotBlank()) listOf(note) else listOf("Community crowdsourced feedback"),
      safetyTipsBn = if (note.isNotBlank()) listOf(note) else listOf("কমিউনিটি নাগরিকদের তথ্য")
    )
    _uiState.update {
      it.copy(
        areaReports = listOf(newReport) + it.areaReports,
        isReportingDialogVisible = false
      )
    }
  }

  // Backward compatibility
  fun joinCommunityWithPin() { verifyPin() }
  fun toggleCommunityJoin(communityId: String) {}
  fun showCreateGroupDialog(show: Boolean) { showCreatePostDialog(show) }
  fun updateNewGroupName(name: String) { updateNewPostTitle(name) }
  fun updateNewGroupNameBn(name: String) {}
  fun updateNewGroupArea(area: String) { updateNewPostLocation(area) }
  fun updateNewGroupPin(pin: String) {}
  fun updateNewGroupDesc(desc: String) { updateNewPostContent(desc) }
  fun createCommunity() { submitNewPost() }
  fun updateChatInput(text: String) { updateReplyInput(text) }
  fun sendCommunityChatMessage(communityId: String = "ramisa_central") { submitReply() }
  fun setFilter(filter: String) { setSafeZoneFilter(filter) }
}
