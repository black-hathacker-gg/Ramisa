package com.example.domain.model

enum class EvidenceType {
  AUDIO_RECORDING,
  INCIDENT_NOTE,
  LOCATION_SNAPSHOT,
  DISPATCH_LOG
}

data class VaultEvidence(
  val id: String,
  val title: String,
  val type: EvidenceType,
  val timestamp: Long = System.currentTimeMillis(),
  val details: String,
  val isEncrypted: Boolean = true,
  val fileSizeBytes: Long = 1024L
)
