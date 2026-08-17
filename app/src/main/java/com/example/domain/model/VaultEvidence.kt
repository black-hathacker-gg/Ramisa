package com.example.domain.model

enum class EvidenceType {
  AUDIO_RECORDING,
  INCIDENT_NOTE,
  LOCATION_SNAPSHOT,
  DISPATCH_LOG,
  CAMERA_SNAPSHOT
}

data class VaultEvidence(
  val id: String,
  val title: String,
  val type: EvidenceType,
  val timestamp: Long = System.currentTimeMillis(),
  val details: String,
  val isEncrypted: Boolean = true,
  val fileSizeBytes: Long = 1024L,
  val sha256Hash: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  val isSyncedToCloud: Boolean = true,
  val isDuressHidden: Boolean = false
)
