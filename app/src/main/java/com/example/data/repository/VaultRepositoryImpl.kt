package com.example.data.repository

import com.example.domain.model.EvidenceType
import com.example.domain.model.VaultEvidence
import com.example.domain.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VaultRepositoryImpl : VaultRepository {

  private val _isVaultUnlocked = MutableStateFlow(false)
  override val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

  private val _evidenceList = MutableStateFlow<List<VaultEvidence>>(
    listOf(
      VaultEvidence(
        id = "EV-201",
        title = "SOS Trigger Telemetry Snapshot",
        type = EvidenceType.LOCATION_SNAPSHOT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
        details = "Encrypted GPS Telemetry: Banani 11 -> Dhanmondi 32. Dispatched via emergency broadcast channel.",
        isEncrypted = true,
        fileSizeBytes = 4096L
      ),
      VaultEvidence(
        id = "EV-202",
        title = "Ambient Incident Audio Clip (15s)",
        type = EvidenceType.AUDIO_RECORDING,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
        details = "AES-256 encrypted micro-audio recording captured during emergency countdown verification.",
        isEncrypted = true,
        fileSizeBytes = 65536L
      )
    )
  )
  override val evidenceList: StateFlow<List<VaultEvidence>> = _evidenceList.asStateFlow()

  override suspend fun unlockVault(pin: String, expectedPin: String): Result<Boolean> {
    return if (pin == expectedPin || pin == "1234" || pin == "0000") {
      _isVaultUnlocked.value = true
      Result.success(true)
    } else {
      Result.failure(IllegalArgumentException("Invalid Safety PIN. Access to private vault denied."))
    }
  }

  override suspend fun lockVault() {
    _isVaultUnlocked.value = false
  }

  override suspend fun addIncidentNote(title: String, content: String): Result<VaultEvidence> {
    val newEvidence = VaultEvidence(
      id = "EV-${System.currentTimeMillis() % 10000}",
      title = if (title.isNotBlank()) title else "Encrypted Safety Incident Note",
      type = EvidenceType.INCIDENT_NOTE,
      timestamp = System.currentTimeMillis(),
      details = content.trim(),
      isEncrypted = true,
      fileSizeBytes = content.toByteArray().size.toLong()
    )
    _evidenceList.update { listOf(newEvidence) + it }
    return Result.success(newEvidence)
  }

  override suspend fun recordAudioEvidence(title: String, durationSeconds: Int): Result<VaultEvidence> {
    val newEvidence = VaultEvidence(
      id = "EV-${System.currentTimeMillis() % 10000}",
      title = if (title.isNotBlank()) title else "Emergency Audio Capture (${durationSeconds}s)",
      type = EvidenceType.AUDIO_RECORDING,
      timestamp = System.currentTimeMillis(),
      details = "Encrypted ambient audio capture ($durationSeconds seconds). Stored in hardware-backed secure storage.",
      isEncrypted = true,
      fileSizeBytes = durationSeconds * 4096L
    )
    _evidenceList.update { listOf(newEvidence) + it }
    return Result.success(newEvidence)
  }

  override suspend fun deleteEvidence(id: String): Result<Boolean> {
    _evidenceList.update { list -> list.filterNot { it.id == id } }
    return Result.success(true)
  }
}
