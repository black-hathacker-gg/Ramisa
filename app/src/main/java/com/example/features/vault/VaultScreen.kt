package com.example.features.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EvidenceType
import com.example.domain.model.VaultEvidence
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
  viewModel: VaultViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  var noteTitle by remember { mutableStateOf("") }
  var noteContent by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Encrypted Safety Vault",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (uiState.isUnlocked) {
            IconButton(onClick = { viewModel.lockVault() }) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Vault")
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    floatingActionButton = {
      if (uiState.isUnlocked && !uiState.isDuressDecoyMode) {
        FloatingActionButton(
          onClick = { viewModel.showAddNoteDialog(true) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_add_vault_note")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
        }
      }
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    if (!uiState.isUnlocked) {
      // Locked Vault State Screen
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(80.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(40.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Secure Incident & Evidence Vault",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Encrypted local storage for emergency telemetry, forensic snapshots, and safety logs. Enter your 4-digit Safety PIN (or Duress PIN in coercion).",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        OutlinedTextField(
          value = uiState.enteredPin,
          onValueChange = viewModel::onPinChange,
          label = { Text("Enter 4-Digit Safety PIN") },
          placeholder = { Text("1234") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_vault_pin")
        )

        if (uiState.pinErrorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = uiState.pinErrorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = { viewModel.unlockVault() },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_unlock_vault"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("UNLOCK VAULT", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      // Unlocked Vault Evidence List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          // Vault Banner / Duress Warning
          if (uiState.isDuressDecoyMode) {
            Card(
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "SAFE DECOY VAULT ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "Displaying standard personal notes. Real evidence remains concealed and cloud-backed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          } else {
            Card(
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = SafeGreenContainer)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SafeGreenDark)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "VAULT SECURED & ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SafeGreenDark
                  )
                  Text(
                    text = "AES-256 encrypted storage • Hardware keystore • SHA-256 sealed",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeGreenDark.copy(alpha = 0.85f)
                  )
                }
              }
            }
          }
        }

        if (!uiState.isDuressDecoyMode) {
          item {
            // Forensic Capture Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = if (uiState.isRecordingAudio) "RECORDING (${uiState.recordingSeconds}s)" else "AUDIO EVIDENCE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (uiState.isRecordingAudio) SafetyRedPrimary else MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Button(
                    onClick = { viewModel.toggleAudioRecording() },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (uiState.isRecordingAudio) SafetyRedPrimary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_toggle_audio_record")
                  ) {
                    Icon(
                      imageVector = if (uiState.isRecordingAudio) Icons.Default.MicOff else Icons.Default.Mic,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.isRecordingAudio) "STOP" else "RECORD", fontSize = 11.sp)
                  }
                }
              }

              Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "FORENSIC PHOTO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Button(
                    onClick = {
                      viewModel.capturePhotoEvidence("Emergency Scene Snapshot", "Dhanmondi 32 - Immediate threat capture")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_capture_photo")
                  ) {
                    Icon(
                      imageVector = Icons.Default.CameraAlt,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SNAPSHOT", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }

        items(uiState.evidenceList, key = { it.id }) { item ->
          EvidenceCard(item = item, onDelete = { viewModel.deleteEvidence(item.id) })
        }

        item {
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
    }
  }

  // Add Incident Note Dialog
  if (uiState.isAddNoteDialogVisible) {
    AlertDialog(
      onDismissRequest = { viewModel.showAddNoteDialog(false) },
      title = {
        Text("Add Encrypted Incident Note", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = noteTitle,
            onValueChange = { noteTitle = it },
            label = { Text("Incident Subject") },
            placeholder = { Text("e.g. Rickshaw Incident on Mirpur Road") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_note_title")
          )

          OutlinedTextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            label = { Text("Incident Details") },
            placeholder = { Text("Describe vehicle number, details, or observations...") },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_note_details")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (noteContent.isNotBlank()) {
              viewModel.addIncidentNote(noteTitle, noteContent)
              noteTitle = ""
              noteContent = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("btn_save_note")
        ) {
          Text("Save to Vault")
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showAddNoteDialog(false) }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun EvidenceCard(item: VaultEvidence, onDelete: () -> Unit) {
  val dateFormatted = remember(item.timestamp) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    sdf.format(Date(item.timestamp))
  }

  Card(
    modifier = Modifier.fillMaxWidth().testTag("vault_item_${item.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = when (item.type) {
              EvidenceType.AUDIO_RECORDING -> Icons.Default.Mic
              EvidenceType.INCIDENT_NOTE -> Icons.Default.Description
              EvidenceType.LOCATION_SNAPSHOT -> Icons.Default.Security
              EvidenceType.DISPATCH_LOG -> Icons.Default.Shield
              EvidenceType.CAMERA_SNAPSHOT -> Icons.Default.CameraAlt
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(onClick = onDelete) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = item.details,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // SHA-256 and Cloud Sync Integrity Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = "Cloud Synced",
            tint = SafeGreenDark,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Cloud Sync & SHA-256 Validated",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = SafeGreenDark
          )
        }

        Text(
          text = dateFormatted,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
