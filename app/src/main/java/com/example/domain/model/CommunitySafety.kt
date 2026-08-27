package com.example.domain.model

enum class CommunityTopicCategory(
  val id: String,
  val titleEn: String,
  val titleBn: String
) {
  ALL("all", "All Discussions", "সকল আলোচনা"),
  EMERGENCY_SOS("sos", "Emergency SOS", "জরুরি অ্যালার্ট"),
  SISTERHOOD_GENERAL("general", "Sisterhood & Sharing", "নারী ফোরাম ও অভিজ্ঞতা"),
  HARASSMENT_ALERT("harassment", "Harassment & Danger Reports", "ইভটিজিং ও বিপদবার্তা"),
  LEGAL_CYBER_HELP("legal", "Legal & Cyber Aid", "আইনি ও সাইবার সহায়তা"),
  HEALTH_WELLNESS("health", "Health & Mental Wellness", "নারী স্বাস্থ্য ও মানসিক সাপোর্ট"),
  SAFE_WALK_BUDDY("safe_walk", "Safe-Walk Companions", "নিরাপদ সঙ্গ ও সহযাত্রী")
}

data class CommunityReply(
  val id: String,
  val postId: String,
  val authorName: String,
  val authorRole: String = "Verified Sister",
  val isAnonymous: Boolean = false,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val likes: Int = 0
)

data class CentralCommunityPost(
  val id: String,
  val category: CommunityTopicCategory = CommunityTopicCategory.SISTERHOOD_GENERAL,
  val authorName: String,
  val authorRole: String = "Verified Member",
  val authorPhone: String = "+880 1712-345678",
  val isAnonymous: Boolean = false,
  val title: String,
  val content: String,
  val locationTag: String = "Dhaka",
  val timestamp: Long = System.currentTimeMillis(),
  val supportCount: Int = 0,
  val helpfulCount: Int = 0,
  val userSupported: Boolean = false,
  val userHelpful: Boolean = false,
  val replies: List<CommunityReply> = emptyList(),
  val isSosAlert: Boolean = false,
  val sosDetails: CommunitySosAlert? = null,
  val isOfflineQueued: Boolean = false
)

data class CentralCommunityInfo(
  val name: String = "RAMISA Central Sisterhood & Safety Community",
  val nameBn: String = "রমিসা সেন্ট্রাল নারী সুরক্ষা ও সহায়তা ফোরাম",
  val description: String = "All-in-one verified crisis response & women's discussion community. Share experiences, get legal/health advice, find safe travel companions, and broadcast instant SOS alerts.",
  val descriptionBn: String = "একক কেন্দ্রীয় নারী সুরক্ষা ও সহায়তা প্ল্যাটফর্ম। জরুরি এসওএস ব্রডকাস্ট, ইভটিজিং প্রতিরোধ, সাইবার সহায়তা, মানসিক স্বাস্থ্য ও নিরাপদ যাতায়াতের উন্মুক্ত ফোরাম।",
  val totalMembers: Int = 2480,
  val activeOnline: Int = 142,
  val accessPin: String = "1234",
  val isUnlocked: Boolean = true
)

data class AreaSafetyReport(
  val id: String,
  val areaName: String,
  val areaNameBn: String,
  val district: String = "Dhaka",
  val safetyScore: Int, // 1 - 100
  val lightingRating: Float, // 1.0 - 5.0
  val policePatrolRating: Float, // 1.0 - 5.0
  val crowdDensity: String, // "High", "Moderate", "Isolated"
  val crowdDensityBn: String,
  val totalReviews: Int,
  val lastReportedMinutesAgo: Int,
  val isCautionZone: Boolean = false,
  val safetyTips: List<String> = emptyList(),
  val safetyTipsBn: List<String> = emptyList()
)

data class LiveShareSession(
  val trackingCode: String,
  val shareableWebUrl: String,
  val isSessionActive: Boolean = true,
  val expiresAtTimestamp: Long,
  val lastKnownLatitude: Double = 23.7258,
  val lastKnownLongitude: Double = 90.3976,
  val batteryLevel: Int = 88
)

data class CommunityGroup(
  val id: String,
  val name: String,
  val nameBn: String,
  val description: String,
  val joinPin: String,
  val locationTag: String,
  val memberCount: Int,
  val isVerified: Boolean = true,
  val creatorName: String = "Admin",
  val creatorPhone: String = "+880 1711-223344",
  val isJoined: Boolean = true,
  val activeSosCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis()
)

data class CommunitySosAlert(
  val id: String,
  val communityId: String = "ramisa_central",
  val communityName: String = "RAMISA Central Community",
  val victimName: String,
  val victimPhone: String,
  val victimBloodGroup: String = "B+",
  val triggerTypeLabel: String,
  val locationName: String,
  val latitude: Double,
  val longitude: Double,
  val mapUrl: String,
  val timestamp: Long = System.currentTimeMillis(),
  val customNote: String = "",
  val respondingCount: Int = 0,
  val respondingMembers: List<String> = emptyList(),
  val status: String = "CRITICAL_ACTIVE" // "CRITICAL_ACTIVE", "RESPONDING", "RESOLVED"
)

data class CommunityMember(
  val id: String,
  val name: String,
  val phone: String,
  val role: String, // "Circle Admin", "Volunteer Responder", "Member", "Legal Advisor"
  val status: String, // "Safe & Active", "On Patrol", "Nearby"
  val isOnline: Boolean = true,
  val avatarInitial: String = "S"
)

data class CommunityChatMessage(
  val id: String,
  val communityId: String = "ramisa_central",
  val senderName: String,
  val senderPhone: String,
  val message: String,
  val isSosAlert: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)
