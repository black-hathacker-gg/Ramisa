package com.example.domain.model

enum class UserVerificationStatus(val labelEn: String, val labelBn: String) {
  PENDING("Pending Verification", "ভেরিফিকেশন পেন্ডিং"),
  VERIFIED("Verified Citizen/Student", "যাচাইকৃত শিক্ষার্থী/নাগরিক"),
  REJECTED("Rejected / Invalid ID", "বাতিলকৃত / অকার্যকর আইডি"),
  SUSPENDED("Suspended / Restricted", "স্থগিত / নিষিদ্ধ অ্যাকাউন্ট")
}

enum class AdminRole {
  SUPER_ADMIN,
  POLICE_PROCTOR,
  CAMPUS_VOLUNTEER_MODERATOR
}

data class AdminUserRecord(
  val id: String,
  val name: String,
  val phone: String,
  val email: String,
  val age: Int,
  val bloodGroup: String,
  val verificationStatus: UserVerificationStatus,
  val idType: String, // "National NID", "University Student ID", "College ID", "Passport"
  val idNumber: String,
  val institutionOrArea: String,
  val emergencyGuardianName: String,
  val emergencyGuardianPhone: String,
  val emergencyGuardianRelation: String,
  val secondaryContactName: String = "",
  val secondaryContactPhone: String = "",
  val registeredAt: String,
  val lastActive: String,
  val sosTriggersCount: Int = 0,
  val activeRiskLevel: String = "Normal", // "Normal", "Watchlist", "High Risk"
  val adminNotes: String = "",
  val isOnline: Boolean = true,
  val lastGpsLocation: String = "Dhaka, Bangladesh (23.7275° N, 90.4019° E)",
  val verificationDocumentNote: String = "Student ID Card front & back uploaded and OCR-scanned."
)

data class SafetyBroadcastMessage(
  val id: String,
  val title: String,
  val message: String,
  val priority: String, // "CRITICAL_RED", "YELLOW_CAUTION", "BLUE_INFO"
  val timestamp: String,
  val targetArea: String = "National (All Users)"
)
