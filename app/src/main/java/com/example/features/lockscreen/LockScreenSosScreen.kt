package com.example.features.lockscreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenSosScreen(
  viewModel: LockScreenSosViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Lock Screen & Offline SOS",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Emergency trigger without unlocking device",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_lockscreen_back")) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.testTag("screen_lockscreen_sos")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // Status Banner Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_lockscreen_status"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isLockScreenGuardActive) Color(0xFFFBE9E7) else MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (uiState.isLockScreenGuardActive) SafetyRedPrimary else Color.Gray),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (uiState.isLockScreenGuardActive) Icons.Default.Lock else Icons.Default.Warning,
              contentDescription = null,
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (uiState.isLockScreenGuardActive) "Lock Screen Guard Active" else "Lock Screen Guard Inactive",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (uiState.isLockScreenGuardActive) SafetyRedPrimary else MaterialTheme.colorScheme.onSurface)
            )
            Text(
              text = if (uiState.isLockScreenGuardActive) "SOS notification is visible on your lock screen. 1-tap triggers direct SMS without entering PIN." else "Enable below to trigger emergency SOS when device is locked.",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
          Switch(
            checked = uiState.isLockScreenGuardActive,
            onCheckedChange = { viewModel.toggleLockScreenGuard(context, it) },
            modifier = Modifier.testTag("switch_lockscreen_guard"),
            colors = SwitchDefaults.colors(
              checkedThumbColor = SafetyRedPrimary,
              checkedTrackColor = Color(0xFFFFCDD2)
            )
          )
        }
      }

      // INTERACTIVE LOCK SCREEN SIMULATOR
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_lockscreen_simulator"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF1E293B)
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Interactive Lock Screen Preview",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
            }
            Text(
              text = "DEVICE LOCKED",
              style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Simulated Phone Lock Screen View
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
              .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
              .padding(16.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              // Simulated Clock
              Text(
                text = "08:45",
                style = MaterialTheme.typography.displayMedium.copy(
                  fontWeight = FontWeight.Light,
                  color = Color.White,
                  letterSpacing = 2.sp
                )
              )
              Text(
                text = "Monday, August 17 • Dhaka",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
              )

              Spacer(modifier = Modifier.height(20.dp))

              // Simulated Lock Screen Notification Banner
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("simulated_notification_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(SafetyRedPrimary),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "RAMISA Safety Guard",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                      )
                    }
                    Text(
                      text = "NOW",
                      style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "🔒 Phone Locked • 1-Tap Offline SOS Ready",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                  )
                  Text(
                    text = "Direct GSM SMS to ${uiState.emergencyContacts.size.coerceAtLeast(3)} guardians with GPS link (No internet needed).",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp)
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  // ACTION BUTTONS IN LOCKSCREEN NOTIFICATION
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // TRIGGER OFFLINE SOS
                    Button(
                      onClick = {
                        viewModel.triggerOfflineSosFromLockScreen(context, onTriggerSos)
                      },
                      modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("btn_simulated_lockscreen_sos"),
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
                    ) {
                      Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "🚨 TRIGGER SOS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                      )
                    }

                    // CALL 999
                    OutlinedButton(
                      onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                        context.startActivity(dialIntent)
                      },
                      modifier = Modifier
                        .weight(0.9f)
                        .height(42.dp)
                        .testTag("btn_simulated_call_999"),
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                      Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "999",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "👆 Tap '🚨 TRIGGER SOS' above to test the lockscreen dispatch",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
              )
            }
          }
        }
      }

      // OFFLINE SMS & MESH CAPABILITIES SECTION
      Text(
        text = "Offline & Zero-Internet Protection",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Direct SMS Fallback
          SettingToggleRow(
            icon = Icons.Default.Sms,
            title = "Direct Native SMS Dispatch",
            subtitle = "Sends emergency SMS directly via cellular tower without internet or mobile data.",
            checked = uiState.isOfflineSmsFallbackEnabled,
            onCheckedChange = { viewModel.toggleOfflineSms(it) }
          )

          // Offline BLE Mesh Beacon
          SettingToggleRow(
            icon = Icons.Default.Hub,
            title = "Offline BLE Mesh Distress Beacon",
            subtitle = "Transmits local Bluetooth beacon so nearby RAMISA safety nodes receive distress alert.",
            checked = uiState.isOfflineMeshBeaconEnabled,
            onCheckedChange = { viewModel.toggleOfflineMesh(it) }
          )
        }
      }

      // HARDWARE SHORTCUTS FOR LOCKED PHONE
      Text(
        text = "Hardware Key Triggers When Locked",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Power Button 3x Press
          SettingToggleRow(
            icon = Icons.Default.PowerSettingsNew,
            title = "Triple-Press Power Button SOS",
            subtitle = "Press physical power button 3 times consecutively while phone is locked in pocket.",
            checked = uiState.isPowerButton3xEnabled,
            onCheckedChange = { viewModel.togglePowerButton3x(it) }
          )

          // Volume Key Trigger
          SettingToggleRow(
            icon = Icons.Default.Vibration,
            title = "Volume Key Emergency Squeeze",
            subtitle = "Press & hold Volume Up + Down together for 3 seconds to silently trigger SOS.",
            checked = uiState.isVolumeKeyTriggerEnabled,
            onCheckedChange = { viewModel.toggleVolumeKeyTrigger(it) }
          )
        }
      }

      // EMERGENCY RECIPIENTS OVERVIEW
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Offline SOS Emergency Dispatch Packet",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "When triggered from the lock screen, RAMISA automatically broadcasts:",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• Live GPS coordinates & Google Maps distress URL\n• Medical alert: Asthmatic, Blood Group B+\n• Offline Direct SMS to: Mother (+8801819112233), Father (+8801711223344), Sister (+8801912334455)\n• Local High-Decibel Siren & Strobe if enabled",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  // Success Confirmation Dialog
  if (uiState.showSuccessDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.dismissSuccessDialog() },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Offline SOS Dispatched!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
      },
      text = {
        Text(
          "The Lock Screen SOS trigger successfully dispatched emergency alerts to ${uiState.emergencyContacts.size.coerceAtLeast(3)} guardian contacts with live GPS coordinates.",
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = { viewModel.dismissSuccessDialog() },
          colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
        ) {
          Text("OK")
        }
      }
    )
  }
}

@Composable
private fun SettingToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
    )
  }
}
