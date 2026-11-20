package com.example.features.journey

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TravelMode
import com.example.core.location.EmergencyLocationManager
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeJourneyScreen(
  viewModel: JourneyViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var confirmEnd by rememberSaveable { mutableStateOf(false) }
  val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    viewModel.useCurrentGpsLocation()
  }
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "RAMISA Safe Journey",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      uiState.statusMessage?.let { message ->
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
      }
      if (uiState.isJourneyActive) {
        // Active Journey Tracking Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("active_journey_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SafeGreenContainer)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SafeGreenPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "JOURNEY IN PROGRESS",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  ),
                  color = SafeGreenDark
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SafeGreenDark
              ) {
                Text(
                  text = "${uiState.expectedDurationMinutes} min ETA",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "${uiState.startLocation} to ${uiState.destination}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = SafeGreenDark
            )
            Text(
              text = "Mode: ${uiState.travelMode.name.replace("_", " ")} • Elapsed: ${uiState.elapsedMinutes}m ${uiState.elapsedSeconds % 60}s",
              style = MaterialTheme.typography.bodySmall,
              color = SafeGreenDark.copy(alpha = 0.85f)
            )
            Text(
              text = uiState.activeJourney?.lastLocation?.description()?.let { "Last location: $it" }
                ?: "Waiting for device location",
              style = MaterialTheme.typography.bodySmall,
              color = SafeGreenDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = (uiState.elapsedSeconds / (uiState.expectedDurationMinutes * 60f)).coerceIn(0f, 1f)
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = SafeGreenPrimary,
              trackColor = SafeGreenDark.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { viewModel.completeJourney() },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("btn_complete_journey"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreenDark)
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Arrived · SMS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              OutlinedButton(
                onClick = { viewModel.triggerSafetyCheckPrompt(true) },
                modifier = Modifier
                  .height(48.dp)
                  .testTag("btn_test_safety_check"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CHECK", fontSize = 13.sp)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
              onClick = { confirmEnd = true },
              modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
              Text("End journey", color = SafeGreenDark)
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Safe Journey Config Form
      Text(
        text = "Plan Protected Travel",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "RAMISA will monitor your travel time and trigger an automatic safety check if unexpected delays occur.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Start Location
      OutlinedTextField(
        value = uiState.startLocation,
        onValueChange = viewModel::onStartLocationChanged,
        label = { Text("Starting Location (বর্তমান অবস্থান)") },
        placeholder = { Text("e.g. Mirpur 10, Dhaka") },
        leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
        trailingIcon = {
          IconButton(
            onClick = {
              if (EmergencyLocationManager.hasLocationPermission(context)) viewModel.useCurrentGpsLocation()
              else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.testTag("btn_use_current_gps")
          ) {
            Icon(
              imageVector = Icons.Default.MyLocation,
              contentDescription = "Use Live GPS Location",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_start_loc")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Destination
      OutlinedTextField(
        value = uiState.destination,
        onValueChange = viewModel::onDestinationChanged,
        label = { Text("Destination (গন্তব্য)") },
        placeholder = { Text("e.g. Dhaka University Campus") },
        leadingIcon = { Icon(imageVector = Icons.Default.Navigation, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_destination")
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Travel Mode Selector
      Text(
        text = "TRAVEL MODE (পরিবহন মাধ্যম)",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          TravelMode.RICKSHAW to "Rickshaw",
          TravelMode.CNG_AUTO to "CNG",
          TravelMode.BUS to "Bus",
          TravelMode.WALKING to "Walk"
        ).forEach { (mode, label) ->
          FilterChip(
            selected = uiState.travelMode == mode,
            onClick = { viewModel.onTravelModeSelected(mode) },
            label = { Text(label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Expected Travel Time
      Text(
        text = "EXPECTED TIME (আনুমানিক সময়)",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(15, 30, 45, 60, 90).forEach { mins ->
          FilterChip(
            selected = uiState.expectedDurationMinutes == mins,
            onClick = { viewModel.onDurationChanged(mins) },
            label = { Text("${mins}m", fontSize = 12.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = { viewModel.startJourney() },
        enabled = !uiState.isJourneyActive,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_start_journey"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(imageVector = Icons.Outlined.Shield, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "START SAFE JOURNEY",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (confirmEnd) AlertDialog(
    onDismissRequest = { confirmEnd = false },
    title = { Text("End this journey?") },
    text = { Text("This stops the arrival check and the current trip’s location tracking.") },
    confirmButton = {
      TextButton(onClick = { confirmEnd = false; viewModel.cancelJourney() }) { Text("End journey") }
    },
    dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("Keep tracking") } }
  )

  // Safety Check Prompt Dialog
  if (uiState.isSafetyPromptVisible) {
    AlertDialog(
      onDismissRequest = { viewModel.triggerSafetyCheckPrompt(false) },
      icon = {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = SafeGreenPrimary,
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(
          text = "Proactive Safety Check",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Are you safe and on your expected route?",
            style = MaterialTheme.typography.bodyMedium
          )
          Text(
            text = "Check-in closes in ${uiState.promptCountdownSeconds} seconds. Use ‘I need help’ to open SOS.",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = SafetyRedLight
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.confirmSafetyCheck(safe = true) },
          colors = ButtonDefaults.buttonColors(containerColor = SafeGreenPrimary),
          modifier = Modifier.testTag("btn_confirm_safe_check")
        ) {
          Text("Yes, I Am Safe")
        }
      },
      dismissButton = {
        Button(
          onClick = {
            viewModel.confirmSafetyCheck(safe = false, onTriggerSos = onTriggerSos)
          },
          colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
          modifier = Modifier.testTag("btn_trigger_sos_from_check")
        ) {
          Text("I Need Help!")
        }
      }
    )
  }
}
