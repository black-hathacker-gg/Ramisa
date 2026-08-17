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
  val id: String = "usr_ramisa_8801",
  val name: String = "Sadia Rahman",
  val phone: String = "+880 1712-345678",
  val ageCategory: AgeCategory = AgeCategory.ADULT_18_PLUS,
  val userType: UserType = UserType.ADULT,
  val emergencyPin: String = "1234",
  val bloodGroup: String = "B+",
  val emergencyNote: String = "Asthmatic; carries emergency inhaler. In case of emergency, contact Amma or Sister immediately.",
  val isChildModeActive: Boolean = false,
  val guardianName: String? = "Rahman Ali (Father)",
  val guardianPhone: String? = "+880 1711-223344",
  val createdAt: Long = System.currentTimeMillis() - 86400000L * 30, // 30 days ago
  val updatedAt: Long = System.currentTimeMillis()
)

data class AuthSession(
  val user: UserProfile,
  val token: String,
  val expiresAt: Long
)
