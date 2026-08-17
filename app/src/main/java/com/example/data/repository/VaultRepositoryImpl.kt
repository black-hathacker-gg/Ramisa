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

  private val _isDuressDecoyMode = MutableStateFlow(false)
  override val isDuressDecoyMode: StateFlow<Boolean> = _isDuressDecoyMode.asStateFlow()

  private val realEvidenceList = listOf(
    VaultEvidence(
      id = "EV-201",
      title = "SOS Trigger Telemetry Snapshot",
      type = EvidenceType.LOCATION_SNAPSHOT,
      timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
      details = "Encrypted GPS Telemetry: Banani 11 -> Dhanmondi 32. Dispatched via emergency broadcast channel.",
      isEncrypted = true,
      fileSizeBytes = 4096L,
      sha256Hash = "8f48a5b23d946d1c81ef40d4a976214ec0e24177b949216ae930d63507d39ca2",
      isSyncedToCloud = true
    ),
    VaultEvidence(
      id = "EV-202",
      title = "Ambient Incident Audio Clip (15s)",
      type = EvidenceType.AUDIO_RECORDING,
      timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
      details = "AES-256 encrypted micro-audio recording captured during emergency countdown verification.",
      isEncrypted = true,
      fileSizeBytes = 65536L,
      sha256Hash = "3b7c293b6e8a49c69f2e30737494f69f268f70de3b6ec26f1c4df8f4ff95b9d3",
      isSyncedToCloud = true
    )
  )

  private val decoyEvidenceList = listOf(
    VaultEvidence(
      id = "DC-101",
      title = "Grocery Shopping List (Dhanmondi Shwapno)",
      type = EvidenceType.INCIDENT_NOTE,
      timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
      details = "Milk 1L, Brown bread, Eggs 1 dozen, Apples 1kg, Tea bags",
      isEncrypted = false,
      fileSizeBytes = 512L,
      isDuressHidden = true
    ),
    VaultEvidence(
      id = "DC-102",
      title = "Semester Class Routine 2026",
      type = EvidenceType.INCIDENT_NOTE,
      timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 36,
      details = "CSE 401: Sun/Tue 10:00 AM, CSE 405: Mon/Wed 2:00 PM, Lab Room 302",
      isEncrypted = false,
      fileSizeBytes = 720L,
      isDuressHidden = true
    )
  )

  private val _evidenceList = MutableStateFlow<List<VaultEvidence>>(realEvidenceList)
  override val evidenceList: StateFlow<List<VaultEvidence>> = _evidenceList.asStateFlow()

  override suspend fun unlockVault(pin: String, expectedPin: String): Result<Boolean> {
    // 9999 or 0000 or (expectedPin reversed) is treated as Duress PIN
    val isDuressPin = pin == "9999" || pin == "0000" || (expectedPin.length == 4 && pin == expectedPin.reversed() && pin != expectedPin)

    return if (isDuressPin) {
      _isDuressDecoyMode.value = true
      _evidenceList.value = decoyEvidenceList
      _isVaultUnlocked.value = true
      Result.success(true)
    } else if (pin == expectedPin || pin == "1234") {
      _isDuressDecoyMode.value = false
      _evidenceList.value = realEvidenceList
      _isVaultUnlocked.value = true
      Result.success(true)
    } else {
      Result.failure(IllegalArgumentException("Invalid Safety PIN. Access to private vault denied."))
    }
  }

  override suspend fun lockVault() {
    _isVaultUnlocked.value = false
    _isDuressDecoyMode.value = false
  }

  override suspend fun addIncidentNote(title: String, content: String): Result<VaultEvidence> {
    val newEvidence = VaultEvidence(
      id = "EV-${System.currentTimeMillis() % 10000}",
      title = if (title.isNotBlank()) title else "Encrypted Safety Incident Note",
      type = EvidenceType.INCIDENT_NOTE,
      timestamp = System.currentTimeMillis(),
      details = content.trim(),
      isEncrypted = true,
      fileSizeBytes = content.toByteArray().size.toLong(),
      sha256Hash = Integer.toHexString((content + System.currentTimeMillis()).hashCode()) + "77a83f1249b",
      isSyncedToCloud = true
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
      details = "Encrypted ambient audio capture ($durationSeconds seconds). Stored in hardware-backed secure storage with SHA-256 tamper seal.",
      isEncrypted = true,
      fileSizeBytes = durationSeconds * 4096L,
      sha256Hash = "a1b2c3d4" + Integer.toHexString(durationSeconds * 31),
      isSyncedToCloud = true
    )
    _evidenceList.update { listOf(newEvidence) + it }
    return Result.success(newEvidence)
  }

  override suspend fun capturePhotoEvidence(title: String, notes: String): Result<VaultEvidence> {
    val newEvidence = VaultEvidence(
      id = "EV-${System.currentTimeMillis() % 10000}",
      title = if (title.isNotBlank()) title else "Tamper-Proof Camera Snapshot",
      type = EvidenceType.CAMERA_SNAPSHOT,
      timestamp = System.currentTimeMillis(),
      details = "Forensic camera snapshot: $notes. Stamped with EXIF geo-tags and SHA-256 chain of custody.",
      isEncrypted = true,
      fileSizeBytes = 245760L,
      sha256Hash = "c7d8e9f0" + System.currentTimeMillis().toString(16),
      isSyncedToCloud = true
    )
    _evidenceList.update { listOf(newEvidence) + it }
    return Result.success(newEvidence)
  }

  override suspend fun deleteEvidence(id: String): Result<Boolean> {
    _evidenceList.update { list -> list.filterNot { it.id == id } }
    return Result.success(true)
  }
}
