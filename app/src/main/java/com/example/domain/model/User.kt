package com.example.domain.model

enum class UserType {
  ADULT,
  CHILD,
  GUARDIAN
}

enum class AgeCategory(val label: String) {
  CHILD_UNDER_12("Child (Under 12 years)"),
  TEEN_13_17("Teen (13 - 17 years)"),
  ADULT_18_PLUS("Adult (18+ years)"),
  SENIOR("Senior Citizen (60+ years)")
}

data class UserProfile(
  val id: String = "local_profile",
  val name: String = "RAMISA User",
  val phone: String = "",
  val ageCategory: AgeCategory = AgeCategory.ADULT_18_PLUS,
  val userType: UserType = UserType.ADULT,
  val emergencyPin: String = "",
  val bloodGroup: String = "Unknown",
  val emergencyNote: String = "",
  val isChildModeActive: Boolean = false,
  val guardianName: String? = null,
  val guardianPhone: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

data class AuthSession(
  val user: UserProfile,
  val token: String,
  val expiresAt: Long
)
