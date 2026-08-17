package com.example.features.hardware

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareTriggerType
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareTriggerScreen(
  viewModel: HardwareTriggerViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.triggerTriggeredSos) {
    if (uiState.triggerTriggeredSos) {
      viewModel.clearSosTriggerFlag()
      onTriggerSos()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Hardware & Wearable Triggers",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))

        // Info Banner
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SafeGreenPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = SafeGreenPrimary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "DISCREET IN-POCKET TRIGGERS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SafeGreenDark
              )
              Text(
                text = "Trigger emergency SOS without unlocking the screen or looking at the phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 1. Power Button Triple-Press
      item {
        TriggerConfigCard(
          title = "Power Button 3x Quick Press",
          subtitle = "Press the power lock key rapidly 3 times to initiate silent SOS broadcast.",
          icon = Icons.Default.PowerSettingsNew,
          isChecked = uiState.config.isPowerTriplePressEnabled,
          onCheckedChange = { viewModel.togglePowerTriplePress(it) },
          onSimulateTest = { viewModel.simulateHardwareTrigger(HardwareTriggerType.POWER_BUTTON_TRIPLE_PRESS) }
        )
      }

      // 2. Volume Rocker In-Pocket Hold
      item {
        TriggerConfigCard(
          title = "Volume Key Discreet Shortcut",
          subtitle = "Hold both volume buttons for 3 seconds or double tap volume-down in pocket.",
          icon = Icons.Default.VolumeUp,
          isChecked = uiState.config.isVolumeShortcutEnabled,
          onCheckedChange = { viewModel.toggleVolumeShortcut(it) },
          onSimulateTest = { viewModel.simulateHardwareTrigger(HardwareTriggerType.VOLUME_ROCKER_HOLD_OR_DOUBLE_TAP) }
        )
      }

      // 3. Shake & High G-Force Gesture
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE65100).copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Discreet Shake Trigger",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = "Vigorously shake phone to trigger SOS",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = uiState.config.isShakeTriggerEnabled,
                onCheckedChange = { viewModel.toggleShakeTrigger(it) }
              )
            }

            AnimatedVisibility(visible = uiState.config.isShakeTriggerEnabled) {
              Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                  text = "Shake Sensitivity: ${String.format("%.1f", uiState.config.shakeSensitivity)}G",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                  value = uiState.config.shakeSensitivity,
                  onValueChange = { viewModel.updateShakeSensitivity(it) },
                  valueRange = 1.5f..4.5f
                )
                OutlinedButton(
                  onClick = { viewModel.simulateHardwareTrigger(HardwareTriggerType.SHAKE_GESTURE_DISCREET) },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Test Shake Gesture Trigger", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }

      // 4. Wear OS / BLE Smart Band Companion
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.config.isWearCompanionConnected) SafeGreenPrimary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (uiState.config.isWearCompanionConnected) {
            androidx.compose.foundation.BorderStroke(1.dp, SafeGreenPrimary)
          } else null
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (uiState.config.isWearCompanionConnected) SafeGreenPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (uiState.config.isWearCompanionConnected) Icons.Default.BluetoothConnected else Icons.Default.Watch,
                    contentDescription = null,
                    tint = if (uiState.config.isWearCompanionConnected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Wearable Smart Band SOS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = if (uiState.config.isWearCompanionConnected) "Paired: ${uiState.config.wearDeviceName}" else "Pair Bluetooth smart band / watch",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (uiState.config.isWearCompanionConnected) SafeGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = uiState.config.isWearCompanionConnected,
                onCheckedChange = { viewModel.toggleWearCompanion(it) }
              )
            }

            if (uiState.config.isWearCompanionConnected) {
              Spacer(modifier = Modifier.height(10.dp))
              OutlinedButton(
                onClick = { viewModel.simulateHardwareTrigger(HardwareTriggerType.WEARABLE_BLUETOOTH_BEACON) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Watch, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Smart Band SOS Press", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Recent Hardware Trigger Telemetry Logs
      if (uiState.recentTriggerEvents.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Recent Trigger Event History",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }

        items(uiState.recentTriggerEvents, key = { it.id }) { event ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SafeGreenPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = event.description,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = event.descriptionBn,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun TriggerConfigCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  onSimulateTest: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Switch(
          checked = isChecked,
          onCheckedChange = onCheckedChange
        )
      }

      if (isChecked) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onSimulateTest,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Simulate $title Test", fontSize = 12.sp)
        }
      }
    }
  }
}
