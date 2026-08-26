package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AdminUserRecord
import com.example.domain.model.UserVerificationStatus

@Entity(tableName = "admin_users")
data class AdminUserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val email: String,
  val age: Int,
  val bloodGroup: String,
  val verificationStatus: String,
  val idType: String,
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
  val activeRiskLevel: String = "Normal",
  val adminNotes: String = "",
  val isOnline: Boolean = true,
  val lastGpsLocation: String = "Dhaka, Bangladesh",
  val verificationDocumentNote: String = ""
)

fun AdminUserRecord.toEntity(): AdminUserEntity {
  return AdminUserEntity(
    id = id,
    name = name,
    phone = phone,
    email = email,
    age = age,
    bloodGroup = bloodGroup,
    verificationStatus = verificationStatus.name,
    idType = idType,
    idNumber = idNumber,
    institutionOrArea = institutionOrArea,
    emergencyGuardianName = emergencyGuardianName,
    emergencyGuardianPhone = emergencyGuardianPhone,
    emergencyGuardianRelation = emergencyGuardianRelation,
    secondaryContactName = secondaryContactName,
    secondaryContactPhone = secondaryContactPhone,
    registeredAt = registeredAt,
    lastActive = lastActive,
    sosTriggersCount = sosTriggersCount,
    activeRiskLevel = activeRiskLevel,
    adminNotes = adminNotes,
    isOnline = isOnline,
    lastGpsLocation = lastGpsLocation,
    verificationDocumentNote = verificationDocumentNote
  )
}

fun AdminUserEntity.toDomain(): AdminUserRecord {
  val status = try {
    UserVerificationStatus.valueOf(verificationStatus)
  } catch (e: Exception) {
    UserVerificationStatus.PENDING
  }

  return AdminUserRecord(
    id = id,
    name = name,
    phone = phone,
    email = email,
    age = age,
    bloodGroup = bloodGroup,
    verificationStatus = status,
    idType = idType,
    idNumber = idNumber,
    institutionOrArea = institutionOrArea,
    emergencyGuardianName = emergencyGuardianName,
    emergencyGuardianPhone = emergencyGuardianPhone,
    emergencyGuardianRelation = emergencyGuardianRelation,
    secondaryContactName = secondaryContactName,
    secondaryContactPhone = secondaryContactPhone,
    registeredAt = registeredAt,
    lastActive = lastActive,
    sosTriggersCount = sosTriggersCount,
    activeRiskLevel = activeRiskLevel,
    adminNotes = adminNotes,
    isOnline = isOnline,
    lastGpsLocation = lastGpsLocation,
    verificationDocumentNote = verificationDocumentNote
  )
}
