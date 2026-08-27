package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserProfile
import com.example.domain.model.UserType

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val ageCategory: String,
  val userType: String,
  val emergencyPin: String,
  val bloodGroup: String,
  val emergencyNote: String,
  val isChildModeActive: Boolean,
  val guardianName: String?,
  val guardianPhone: String?,
  val createdAt: Long,
  val updatedAt: Long,
  val isLoggedIn: Boolean = true
)

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        phone = phone,
        ageCategory = ageCategory.name,
        userType = userType.name,
        emergencyPin = emergencyPin,
        bloodGroup = bloodGroup,
        emergencyNote = emergencyNote,
        isChildModeActive = isChildModeActive,
        guardianName = guardianName,
        guardianPhone = guardianPhone,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isLoggedIn = true
    )
}

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        name = name,
        phone = phone,
        ageCategory = AgeCategory.valueOf(ageCategory),
        userType = UserType.valueOf(userType),
        emergencyPin = emergencyPin,
        bloodGroup = bloodGroup,
        emergencyNote = emergencyNote,
        isChildModeActive = isChildModeActive,
        guardianName = guardianName,
        guardianPhone = guardianPhone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
