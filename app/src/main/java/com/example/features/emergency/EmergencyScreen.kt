package com.example.features.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Sms
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
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary

@Composable
fun EmergencyScreen(
  viewModel: EmergencyViewModel,
  onResolveEmergency: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

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
    color = Color(0xFF240618) // Velvet plum-pink emergency canvas
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
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
          .background(SafetyRedPrimary.copy(alpha = 0.25f))
          .border(2.dp, SafetyRedPrimary, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Emergency Alert",
          tint = SafetyRedLight,
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
        color = Color.White,
        modifier = Modifier.testTag("emergency_title")
      )

      Text(
        text = "Alert ID: ${uiState.emergencyEventId} • Active for ${uiState.elapsedSeconds}s",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = SafetyRedLight
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Status Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF381028))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = SafetyRedLight,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = uiState.locationLabel,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = Color.White
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Sms,
              contentDescription = null,
              tint = SafeGreenPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${uiState.contactsNotifiedCount} Trusted contacts notified with GPS location & battery status",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ShareLocation,
              contentDescription = null,
              tint = SafeGreenPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Live telemetry streaming: Lat ${uiState.location.latitude}, Lng ${uiState.location.longitude}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color(0xFFFFB74D),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Lock Screen Guard & Offline Native SMS broadcast active (No internet required)",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = Color(0xFFFFCC80)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

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

      // Call Primary Contact
      OutlinedButton(
        onClick = { dialHotline(context, "+8801819112233") },
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
          text = "CALL PRIMARY CONTACT (আম্মা • +880 1819-112233)",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Instant WhatsApp Emergency Broadcast Share
      Button(
        onClick = {
          val sosMsg = "🚨 [RAMISA SOS ALERT] I am in urgent danger! My live location: https://maps.google.com/?q=${uiState.location.latitude},${uiState.location.longitude} - Please send help immediately!"
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sosMsg)
            type = "text/plain"
          }
          context.startActivity(Intent.createChooser(sendIntent, "Broadcast SOS via WhatsApp / SMS"))
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
          text = "BROADCAST SOS TO WHATSAPP / SMS",
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
            text = "Enter your 4-digit Emergency PIN to confirm you are safe and notify your contacts that the situation is resolved.",
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
