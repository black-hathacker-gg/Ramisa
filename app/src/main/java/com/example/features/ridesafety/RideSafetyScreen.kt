package com.example.features.ridesafety

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MinorCrash
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSafetyScreen(
  viewModel: RideSafetyViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Rickshaw & Transit Guard",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Public transit safety & vehicle logger (রিকশা ও বাস গার্ড)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

      // Notification / Feedback Banner
      item {
        AnimatedVisibility(visible = uiState.feedbackMessage != null) {
          uiState.feedbackMessage?.let { msg ->
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              color = if (uiState.isDeviationAlertActive) PinkPrimary else PinkContainer
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = msg,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = if (uiState.isDeviationAlertActive) Color.White else PinkPrimaryDark
                )
                TextButton(onClick = { viewModel.clearFeedback() }) {
                  Text("Dismiss", color = if (uiState.isDeviationAlertActive) Color.White else PinkPrimary)
                }
              }
            }
          }
        }
      }

      // ACTIVE RIDE STATUS CARD OR SETUP FORM
      if (uiState.isRideMonitoringActive) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("active_ride_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PinkContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(PinkPrimary),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "RIDE MONITORING ACTIVE",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = PinkPrimaryDark
                    )
                    Text(
                      text = uiState.transportType.label,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = PinkPrimary
                ) {
                  Text(
                    text = "${uiState.rideDurationMinutes} / ${uiState.maxExpectedMinutes} mins",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              HorizontalDivider(color = PinkPrimary.copy(alpha = 0.2f))

              if (uiState.vehiclePlateNumber.isNotBlank()) {
                Text(
                  text = "Vehicle/Plate: ${uiState.vehiclePlateNumber}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = PinkPrimaryDark
                )
              }
              if (uiState.destination.isNotBlank()) {
                Text(
                  text = "Heading towards: ${uiState.destination}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              // Action buttons during active ride
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { viewModel.endRideMonitoring() },
                  colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("btn_end_ride")
                ) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Arrived Safely")
                }

                OutlinedButton(
                  onClick = { viewModel.simulateRouteDeviation() },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("btn_simulate_deviation")
                ) {
                  Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Test Deviation")
                }
              }

              // Emergency Trigger in Ride
              Button(
                onClick = onTriggerSos,
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimaryDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_ride_emergency_sos")
              ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("EMERGENCY: SUSPICIOUS DRIVER / ROUTE", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        // SETUP RIDE FORM
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("ride_setup_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "1. SELECT VEHICLE TYPE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = PinkPrimary
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                TransportType.values().take(2).forEach { type ->
                  FilterChip(
                    selected = uiState.transportType == type,
                    onClick = { viewModel.setTransportType(type) },
                    label = { Text(type.label.split(" ")[0]) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = PinkPrimary,
                      selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                TransportType.values().drop(2).forEach { type ->
                  FilterChip(
                    selected = uiState.transportType == type,
                    onClick = { viewModel.setTransportType(type) },
                    label = { Text(type.label.split(" ")[0]) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = PinkPrimary,
                      selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                  )
                }
              }

              Text(
                text = "2. LOG VEHICLE DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = PinkPrimary
              )

              OutlinedTextField(
                value = uiState.vehiclePlateNumber,
                onValueChange = { viewModel.updateVehiclePlate(it) },
                label = { Text("Vehicle Number / Rickshaw Plate (নাম্বার)") },
                placeholder = { Text("e.g. Dhaka Metro-Ka 12-3456 or Yellow Rickshaw #42") },
                modifier = Modifier.fillMaxWidth().testTag("input_plate_number")
              )

              OutlinedTextField(
                value = uiState.destination,
                onValueChange = { viewModel.updateDestination(it) },
                label = { Text("Destination (গন্তব্য)") },
                placeholder = { Text("e.g. Dhaka University Central Library") },
                modifier = Modifier.fillMaxWidth().testTag("input_destination")
              )

              Text(
                text = "3. ESTIMATED TRIP TIME: ${uiState.maxExpectedMinutes} MINS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimary
              )

              Slider(
                value = uiState.maxExpectedMinutes.toFloat(),
                onValueChange = { viewModel.updateExpectedTime(it.toInt()) },
                valueRange = 5f..60f,
                steps = 10,
                colors = SliderDefaults.colors(
                  thumbColor = PinkPrimary,
                  activeTrackColor = PinkPrimary
                ),
                modifier = Modifier.testTag("slider_trip_time")
              )

              Button(
                onClick = { viewModel.startRideMonitoring() },
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("btn_start_ride_guard")
              ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("START RIDE GUARD & BROADCAST", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // BANGLADESH PUBLIC TRANSIT SAFETY TIPS
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
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = "TRANSIT SAFETY BEST PRACTICES",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = PinkPrimary
            )

            Text(
              text = "• Rickshaw/CNG: Always sit with your bag between you and the barrier. Avoid holding phones openly on busy roads.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "• Bus: Prefer seats near the front door or women's reserved section. Keep emergency shortcut armed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "• Ride-sharing: Confirm plate number and driver identity before boarding. Share the live tracking link with your circle.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}
