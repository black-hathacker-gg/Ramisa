package com.example.features.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
  viewModel: EmergencyViewModel,
  onResolveEmergency: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Permission Launcher for SMS & Location
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
    if (!smsGranted) {
      // Handled by viewmodel
    }
  }

  LaunchedEffect(Unit) {
    permissionLauncher.launch(
      arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO
      )
    )
  }

  // A pulsing animation for the background map overlay
  val infiniteTransition = rememberInfiniteTransition()
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    )
  )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = SurfaceLight
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // BACKGROUND MAP PLACEHOLDER
      // In a real app this would be a GoogleMap composable with markers
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(PinkContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(150.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(PinkPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Place,
              contentDescription = "Map Marker",
              tint = PinkPrimary,
              modifier = Modifier.size(48.dp)
            )
          }
        }
      }

      // FOREGROUND BOTTOM SHEET OVERLAY
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Bottom
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(32.dp),
          color = SurfaceCardLight,
          shadowElevation = 12.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, PinkBorderSubtle)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Header
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(28.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "EMERGENCY ACTIVE",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = PinkPrimaryDark
              )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = formatTime(uiState.elapsedSeconds),
              style = MaterialTheme.typography.displayMedium,
              color = PinkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "HELP HAS BEEN NOTIFIED",
              style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
              color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              EmergencyStatusCard(
                title = "LOCATION SHARED",
                icon = Icons.Default.ShareLocation,
                modifier = Modifier.weight(1f)
              )
              EmergencyStatusCard(
                title = "CONTACTS ALERTED (${uiState.contactsNotifiedCount})",
                icon = Icons.Default.Contacts,
                modifier = Modifier.weight(1f)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              EmergencyStatusCard(
                title = "RECORDING ACTIVE",
                icon = Icons.Default.Mic,
                modifier = Modifier.weight(1f)
              )
              EmergencyStatusCard(
                title = "999 CALLING",
                icon = Icons.Default.Phone,
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
              onClick = { callPolice(context) },
              modifier = Modifier.fillMaxWidth().height(56.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White),
              shape = RoundedCornerShape(16.dp)
            ) {
              Icon(Icons.Default.Phone, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Call 999", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
              onClick = { viewModel.showResolveDialog(true) },
              modifier = Modifier.fillMaxWidth().height(56.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimaryDark),
              border = androidx.compose.foundation.BorderStroke(1.dp, PinkPrimaryDark),
              shape = RoundedCornerShape(16.dp)
            ) {
              Text("End Emergency", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }

      // Resolve Dialog
      if (uiState.isResolveDialogVisible) {
        AlertDialog(
          onDismissRequest = { viewModel.showResolveDialog(false) },
          title = {
            Text("End Emergency", style = MaterialTheme.typography.titleLarge, color = PinkPrimaryDark)
          },
          text = {
            Column {
              Text("Enter your Secret PIN to disable SOS mode.", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
              Spacer(modifier = Modifier.height(16.dp))
              OutlinedTextField(
                value = uiState.enteredPin,
                onValueChange = { viewModel.onPinEntered(it) },
                label = { Text("Secret PIN") },
                isError = uiState.pinErrorMessage != null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = PinkPrimary,
                  focusedLabelColor = PinkPrimary,
                  cursorColor = PinkPrimary
                )
              )
              if (uiState.pinErrorMessage != null) {
                Text(
                  text = uiState.pinErrorMessage!!,
                  color = PinkPrimaryDark,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          },
          confirmButton = {
            Button(
              onClick = { viewModel.resolveEmergency(onResolved = onResolveEmergency) },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
              Text("Verify & End")
            }
          },
          dismissButton = {
            TextButton(
              onClick = { viewModel.showResolveDialog(false) },
              colors = ButtonDefaults.textButtonColors(contentColor = PinkPrimaryDark)
            ) {
              Text("Cancel")
            }
          },
          containerColor = SurfaceCardLight
        )
      }
    }
  }
}

@Composable
fun EmergencyStatusCard(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.height(84.dp),
    shape = RoundedCornerShape(16.dp),
    color = PinkContainer.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(1.dp, PinkPrimary.copy(alpha = 0.2f))
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(8.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
        color = PinkPrimaryDark,
        textAlign = TextAlign.Center
      )
    }
  }
}

private fun formatTime(seconds: Int): String {
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60
  return if (hours > 0) {
    String.format("%02d:%02d:%02d", hours, minutes, secs)
  } else {
    String.format("%02d:%02d", minutes, secs)
  }
}

private fun callPolice(context: Context) {
  try {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:999")
    context.startActivity(intent)
  } catch (e: Exception) {
    // Graceful fallback
  }
}
