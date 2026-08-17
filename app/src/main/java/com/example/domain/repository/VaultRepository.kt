package com.example.domain.repository

import com.example.domain.model.EvidenceType
import com.example.domain.model.VaultEvidence
import kotlinx.coroutines.flow.StateFlow

interface VaultRepository {
  val isVaultUnlocked: StateFlow<Boolean>
  val isDuressDecoyMode: StateFlow<Boolean>
  val evidenceList: StateFlow<List<VaultEvidence>>

  suspend fun unlockVault(pin: String, expectedPin: String): Result<Boolean>
  suspend fun lockVault()
  suspend fun addIncidentNote(title: String, content: String): Result<VaultEvidence>
  suspend fun recordAudioEvidence(title: String, durationSeconds: Int): Result<VaultEvidence>
  suspend fun capturePhotoEvidence(title: String, notes: String): Result<VaultEvidence>
  suspend fun deleteEvidence(id: String): Result<Boolean>
}
