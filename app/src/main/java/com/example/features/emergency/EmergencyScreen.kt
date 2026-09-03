package com.example.features.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.OnPinkContainer

@Composable
fun EmergencyScreen(
  viewModel: EmergencyViewModel,
  onResolveEmergency: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  // Permission Launcher for SMS & Location
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
    if (smsGranted) {
      viewModel.dispatchSimSmsAndCall(initiateCall = false)
    }
  }

  LaunchedEffect(Unit) {
    val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasSms || !hasLocation) {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.SEND_SMS,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  // Pulsing animation for emergency indicator
  val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "emergency_scale"
  )

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background // Pink and white theme background
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Urgent Beacon Badge
      Box(
        modifier = Modifier
          .size(100.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(PinkPrimary.copy(alpha = 0.15f))
          .border(2.dp, PinkPrimary, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Emergency Alert",
          tint = PinkPrimary,
          modifier = Modifier.size(48.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "EMERGENCY SOS ACTIVE",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 1.5.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.testTag("emergency_title")
      )

      Text(
        text = "Alert ID: ${uiState.emergencyEventId} • Active for ${uiState.elapsedSeconds}s",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = PinkPrimary
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Permission Warning Banner if SMS Permission Missing
      if (!uiState.isSmsPermissionGranted) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = PinkPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "SMS Permission Needed",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OnPinkContainer)
              )
              Text(
                text = "Grant SMS permission so emergency SMS can be sent via SIM without internet.",
                style = MaterialTheme.typography.bodySmall.copy(color = OnPinkContainer.copy(alpha = 0.8f))
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                permissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
                  )
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Grant", fontSize = 12.sp)
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // SMS DISPATCH CONFIRMATION PROMPT (Prominent Green Banner with animated Checkmark)
      if (uiState.isSmsSent && uiState.isSmsPermissionGranted) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sms_success_confirmation_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
          border = BorderStroke(1.5.dp, SafeGreenPrimary)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SafeGreenPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Success",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "SMS Successfully Dispatched!",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SafeGreenDark
                  )
                )
                Text(
                  text = "অফলাইন SMS সফলভাবে অভিভাবকদের কাছে পাঠানো হয়েছে",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.DarkGray,
                    fontSize = 11.sp
                  )
                )
              }
            }

            if (uiState.contactedNames.isNotEmpty()) {
              Text(
                text = "Dispatched to ${uiState.contactedNames.size} Emergency Contact(s):",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SafeGreenDark,
                  fontWeight = FontWeight.SemiBold
                )
              )
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(uiState.contactedNames) { name ->
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, SafeGreenPrimary.copy(alpha = 0.6f))
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SafeGreenPrimary,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = SafeGreenDark,
                          fontSize = 12.sp
                        )
                      )
                    }
                  }
                }
              }
            }

            Text(
              text = "SIM Direct Network (Offline) • Live GPS Map Link & Battery Info Attached",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.DarkGray,
                fontSize = 11.sp
              )
            )
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Status Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // GPS Location
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = PinkPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = uiState.locationLabel,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          // Direct SIM SMS Status
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.SimCard,
              contentDescription = null,
              tint = SafeGreenPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Automated SIM SMS (No Internet / MB Needed)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SafeGreenPrimary)
              )
              Text(
                text = uiState.smsStatusSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Live Telemetry
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ShareLocation,
              contentDescription = null,
              tint = SafeGreenPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "GPS Coordinates: ${uiState.location.latitude}, ${uiState.location.longitude}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Offline Resilience
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = PinkPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Lock Screen Guard & Hardware Trigger Active",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Resend SIM SMS Button
      OutlinedButton(
        onClick = { viewModel.dispatchSimSmsAndCall(initiateCall = false) },
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag("btn_resend_sim_sms"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeGreenPrimary)
      ) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "RESEND SIM SMS TO ALL GUARDIANS",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Direct Action Buttons: Bangladesh 999 and Hotlines
      Text(
        text = "RAPID RESPONSE HOTLINES",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = Color.White.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 999 Hotline
      Button(
        onClick = { dialHotline(context, "999") },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("btn_call_999"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
      ) {
        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "CALL 999 (জাতীয় জরুরি সেবা)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Bangladesh Police • Ambulance • Fire Service",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 109 Women & Children Violence Helpline
      Button(
        onClick = { dialHotline(context, "109") },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_call_109"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
      ) {
        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "CALL 109 (নারী ও শিশু নির্যাতন সেল)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "National Helpline for Violence Against Women & Children",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 1098 Child Protection Helpline
      Button(
        onClick = { dialHotline(context, "1098") },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_call_1098"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
      ) {
        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "CALL 1098 (শিশু হেল্পলাইন)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "National Toll-Free Child Protection & Rescue Helpline",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Call Primary Contact (Dynamic from Database)
      val primaryContact = uiState.primaryContact
      val primaryName = primaryContact?.name ?: "Mother (আম্মা)"
      val primaryPhone = primaryContact?.phone ?: "+880 1819-112233"

      OutlinedButton(
        onClick = { dialHotline(context, primaryPhone) },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_call_primary_contact"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
      ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "CALL PRIMARY CONTACT ($primaryName • $primaryPhone)",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Instant WhatsApp Emergency Broadcast Share
      Button(
        onClick = {
          val sosMsg = "[RAMISA SOS ALERT] আমি বিপদে আছি! My live GPS Location: https://maps.google.com/?q=${uiState.location.latitude},${uiState.location.longitude} - Please send help immediately!"
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sosMsg)
            type = "text/plain"
          }
          context.startActivity(Intent.createChooser(sendIntent, "Broadcast SOS via WhatsApp / Other Apps"))
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_broadcast_sos_share"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
      ) {
        Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "SHARE SOS VIA WHATSAPP / OTHER APPS",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )
      }

      Spacer(modifier = Modifier.height(26.dp))

      // Resolve Button
      Button(
        onClick = { viewModel.showResolveDialog(true) },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_resolve_sos"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafeGreenPrimary)
      ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "I AM SAFE • RESOLVE EMERGENCY",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Resolve Confirmation Dialog
  if (uiState.isResolveDialogVisible) {
    AlertDialog(
      onDismissRequest = { viewModel.showResolveDialog(false) },
      title = {
        Text(
          text = "Resolve Emergency Alert",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter your 4-digit Emergency PIN to confirm you are safe.",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = uiState.enteredPin,
            onValueChange = viewModel::onPinEntered,
            label = { Text("Safety PIN") },
            placeholder = { Text("1234") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_resolve_pin")
          )
          if (uiState.pinErrorMessage != null) {
            Text(
              text = uiState.pinErrorMessage ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resolveEmergency(onResolved = onResolveEmergency)
          },
          colors = ButtonDefaults.buttonColors(containerColor = SafeGreenDark),
          modifier = Modifier.testTag("dialog_btn_confirm_resolve")
        ) {
          Text("Confirm Safe")
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showResolveDialog(false) }) {
          Text("Cancel")
        }
      }
    )
  }
}

private fun dialHotline(context: Context, number: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$number")
    }
    context.startActivity(intent)
  } catch (e: Exception) {
    // Graceful fallback for headless/test environments
  }
}
