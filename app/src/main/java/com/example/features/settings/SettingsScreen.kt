package com.example.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  onLogout: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  onNavigateToVault: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  var isChangePinDialogVisible by remember { mutableStateOf(false) }
  var newPinInput by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Settings & Security",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // User Profile Header Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToProfile() }
          .testTag("settings_profile_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "SR",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Sadia Rahman",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "View & Edit Safety Profile, Blood Group, Guardian",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }

          Text(
            text = "VIEW",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Encrypted Vault Quick Access Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToVault() }
          .testTag("settings_vault_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FolderSpecial,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Encrypted Safety Vault",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Access incident recordings & evidence logs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = "OPEN",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Language Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_lang_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Language (ভাষা)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            FilterChip(
              selected = uiState.language == AppLanguage.ENGLISH,
              onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
              label = { Text("English") },
              modifier = Modifier.testTag("chip_lang_en")
            )
            FilterChip(
              selected = uiState.language == AppLanguage.BANGLA,
              onClick = { viewModel.setLanguage(AppLanguage.BANGLA) },
              label = { Text("বাংলা (Bangla)") },
              modifier = Modifier.testTag("chip_lang_bn")
            )
          }
        }
      }

      // Offline & Safety Toggles
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "SAFETY AUTOMATIONS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // SMS Fallback Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Cellular SMS Fallback",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "Automatically prepare SMS when internet is unavailable",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = uiState.isSmsFallbackEnabled,
              onCheckedChange = { viewModel.toggleSmsFallback() },
              modifier = Modifier.testTag("switch_sms_fallback")
            )
          }

          HorizontalDivider()

          // Child Safety Mode Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Child Safety Mode (শিশু সুরক্ষা)",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "Simplified single-tap interface locked with guardian PIN",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = uiState.isChildSafetyModeEnabled,
              onCheckedChange = { viewModel.toggleChildMode() },
              modifier = Modifier.testTag("switch_child_mode")
            )
          }
        }
      }

      // Bangladesh Emergency Hotlines Directory
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Bangladesh Emergency Directory",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          EmergencyHotlineRow("National Emergency (পুলিশ/অ্যাম্বুলেন্স)", "999")
          EmergencyHotlineRow("Women & Child Abuse Prevention (নারী ও শিশু নির্যাতন)", "109")
          EmergencyHotlineRow("Child Helpline (শিশু সুরক্ষা সহায়তা)", "1098")
          EmergencyHotlineRow("National Legal Aid (আইনি সহায়তা)", "16430")
        }
      }

      // Security PIN Management
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isChangePinDialogVisible = true }
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Emergency & Duress PIN",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "PIN is currently active • Tap to update",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = "EDIT",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Tip: Enter '9999' or reversed PIN in the Vault to silently open Decoy Mode under coercion.",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // About & Version
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "RAMISA v5.0 (Phase 5: Hardware & Wearable Triggers)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Women's & Children's Personal Safety System for Bangladesh",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          TextButton(onClick = onLogout, modifier = Modifier.testTag("btn_logout")) {
            Text(
              text = "Log Out / Switch Account",
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Change PIN Dialog
  if (isChangePinDialogVisible) {
    AlertDialog(
      onDismissRequest = { isChangePinDialogVisible = false },
      title = { Text("Update Emergency PIN") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Enter a new 4-digit PIN for resolving emergency SOS alerts.")
          OutlinedTextField(
            value = newPinInput,
            onValueChange = { if (it.length <= 4) newPinInput = it },
            label = { Text("4-Digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_input_new_pin")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPinInput.length == 4) {
              viewModel.updatePin(newPinInput)
              newPinInput = ""
              isChangePinDialogVisible = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Update PIN")
        }
      },
      dismissButton = {
        TextButton(onClick = { isChangePinDialogVisible = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun EmergencyHotlineRow(title: String, number: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = MaterialTheme.colorScheme.primaryContainer
    ) {
      Text(
        text = number,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
      )
    }
  }
}
