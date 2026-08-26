package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.SafetyBroadcastMessage

@Entity(tableName = "safety_broadcasts")
data class SafetyBroadcastEntity(
  @PrimaryKey val id: String,
  val title: String,
  val message: String,
  val priority: String,
  val timestamp: String,
  val targetArea: String
)

fun SafetyBroadcastMessage.toEntity(): SafetyBroadcastEntity {
  return SafetyBroadcastEntity(
    id = id,
    title = title,
    message = message,
    priority = priority,
    timestamp = timestamp,
    targetArea = targetArea
  )
}

fun SafetyBroadcastEntity.toDomain(): SafetyBroadcastMessage {
  return SafetyBroadcastMessage(
    id = id,
    title = title,
    message = message,
    priority = priority,
    timestamp = timestamp,
    targetArea = targetArea
  )
}
