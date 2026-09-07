package com.example.features.journey

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TravelMode
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeJourneyScreen(
  viewModel: JourneyViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "SAFE JOURNEY",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PinkPrimary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = SurfaceLight
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (uiState.isJourneyCompleted) {
        CompletedJourneyView(
          message = uiState.statusMessage,
          onDismiss = { viewModel.dismissCompletedJourney() }
        )
      } else if (uiState.isDeviationAlertTriggered) {
        RouteDeviationView(
          onSafe = { viewModel.confirmSafetyCheck(safe = true) },
          onNeedHelp = { 
             viewModel.confirmSafetyCheck(safe = false, onTriggerSos = onTriggerSos)
          }
        )
      } else if (uiState.isJourneyActive) {
        ActiveJourneyView(
          uiState = uiState,
          onSafe = { viewModel.completeJourney() },
          onEndJourney = { viewModel.cancelJourney() },
          onSimulateDeviation = { viewModel.triggerSafetyCheckPrompt(true) } // For testing
        )
      } else {
        PreJourneyView(
          uiState = uiState,
          viewModel = viewModel,
          scrollState = scrollState
        )
      }
    }
  }
}

@Composable
fun PreJourneyView(
  uiState: JourneyUiState,
  viewModel: JourneyViewModel,
  scrollState: androidx.compose.foundation.ScrollState
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(24.dp)
  ) {
    Text(
      text = "Where are you going?",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
      color = PinkPrimaryDark
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Share your journey with your safety circle.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondaryLight
    )
    
    Spacer(modifier = Modifier.height(32.dp))

    // Location Inputs
    OutlinedTextField(
      value = uiState.startLocation,
      onValueChange = viewModel::onStartLocationChanged,
      label = { Text("Current location") },
      leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = PinkPrimary) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PinkPrimary,
        focusedLabelColor = PinkPrimary,
        cursorColor = PinkPrimary
      ),
      singleLine = true
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = uiState.destination,
      onValueChange = viewModel::onDestinationChanged,
      label = { Text("Destination") },
      leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = PinkPrimary) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PinkPrimary,
        focusedLabelColor = PinkPrimary,
        cursorColor = PinkPrimary
      ),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    // ETA
    Text(
      text = "Expected arrival",
      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
      color = PinkPrimaryDark
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(15, 30, 45, 60).forEach { mins ->
        FilterChip(
          selected = uiState.expectedDurationMinutes == mins,
          onClick = { viewModel.onDurationChanged(mins) },
          label = { Text("${mins} min", fontWeight = FontWeight.SemiBold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PinkPrimary,
            selectedLabelColor = Color.White
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = uiState.expectedDurationMinutes == mins,
            borderColor = PinkBorderSubtle,
            selectedBorderColor = PinkPrimary
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Travel Mode
    Text(
      text = "Travel mode",
      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
      color = PinkPrimaryDark
    )
    Spacer(modifier = Modifier.height(12.dp))
    
    // We only take a subset to fit
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(
        TravelMode.WALKING to "Walking",
        TravelMode.BUS to "Bus",
        TravelMode.CNG_AUTO to "CNG",
        TravelMode.RICKSHAW to "Other"
      ).forEach { (mode, label) ->
        FilterChip(
          selected = uiState.travelMode == mode,
          onClick = { viewModel.onTravelModeSelected(mode) },
          label = { Text(label, fontWeight = FontWeight.SemiBold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PinkPrimary,
            selectedLabelColor = Color.White
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = uiState.travelMode == mode,
            borderColor = PinkBorderSubtle,
            selectedBorderColor = PinkPrimary
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(48.dp))

    Button(
      onClick = { viewModel.startJourney() },
      modifier = Modifier.fillMaxWidth().height(56.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
    ) {
      Text("START SAFE JOURNEY", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
    }
  }
}

@Composable
fun ActiveJourneyView(
  uiState: JourneyUiState,
  onSafe: () -> Unit,
  onEndJourney: () -> Unit,
  onSimulateDeviation: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    // Map Placeholder (in real app, a GoogleMap would be here)
    Box(
      modifier = Modifier.fillMaxWidth().weight(1f).background(PinkContainer.copy(alpha = 0.3f)),
      contentAlignment = Alignment.Center
    ) {
      // Just some decorative lines for animated route feel
      Icon(Icons.Default.Map, contentDescription = null, tint = PinkPrimary.copy(alpha = 0.5f), modifier = Modifier.size(120.dp))
    }

    // Bottom Sheet Content
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
      color = SurfaceCardLight,
      shadowElevation = 16.dp,
      border = BorderStroke(1.dp, PinkBorderSubtle)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(PinkPrimary))
          Spacer(modifier = Modifier.width(12.dp))
          Text("JOURNEY ACTIVE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp), color = PinkPrimary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(uiState.startLocation, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = PinkPrimary, modifier = Modifier.padding(vertical = 4.dp))
            Text(uiState.destination, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PinkPrimaryDark)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("ETA", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
            Text("${uiState.expectedDurationMinutes} min", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PinkPrimary)
          }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val progress = (uiState.elapsedSeconds / (uiState.expectedDurationMinutes * 60f)).coerceIn(0f, 1f)
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
          color = PinkPrimary,
          trackColor = PinkContainer
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Elapsed: ${uiState.elapsedMinutes}m", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
          Text("On planned route", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PinkPrimaryDark)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          OutlinedButton(
            onClick = onEndJourney,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PinkPrimaryDark),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimaryDark)
          ) {
            Text("End Journey", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onSafe,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
          ) {
            Text("I'm Safe", fontWeight = FontWeight.Bold)
          }
        }
        
        // Simulating deviation for test
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Simulate Deviation",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondaryLight,
          modifier = Modifier.clickable { onSimulateDeviation() }.padding(8.dp)
        )
      }
    }
  }

  // Real-time Safety Check Prompt overlay (if triggered normally)
  if (uiState.isSafetyPromptVisible) {
    AlertDialog(
      onDismissRequest = { /* No dismiss allowed */ },
      title = { Text("Route Change Detected", color = PinkPrimaryDark, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
      text = {
        Column {
          Text("Are you safe and on your expected route?", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Escalation in ${uiState.promptCountdownSeconds}s", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PinkPrimary)
        }
      },
      confirmButton = {
        Button(
          onClick = { onSafe() },
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
        ) {
          Text("I'm Safe")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { onSimulateDeviation() }, // Trigger deviation
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimaryDark),
          border = BorderStroke(1.dp, PinkPrimaryDark)
        ) {
          Text("Need Help")
        }
      },
      containerColor = SurfaceCardLight
    )
  }
}

@Composable
fun RouteDeviationView(onSafe: () -> Unit, onNeedHelp: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition()
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f, targetValue = 1.05f,
    animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
  )

  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    // Map with pulse
    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(PinkContainer.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
      Box(modifier = Modifier.size(150.dp).scale(pulseScale).clip(CircleShape).background(PinkPrimary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(48.dp))
      }
    }

    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
      color = SurfaceCardLight,
      shadowElevation = 16.dp,
      border = BorderStroke(1.dp, PinkPrimary)
    ) {
      Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(32.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Text("ROUTE CHANGE", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = PinkPrimaryDark)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "You've moved away from your planned route.",
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondaryLight,
          textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
          onClick = onNeedHelp,
          modifier = Modifier.fillMaxWidth().height(64.dp),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
        ) {
          Text("NEED HELP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
          onClick = onSafe,
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, PinkPrimaryDark),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimaryDark)
        ) {
          Text("I'M SAFE", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CompletedJourneyView(message: String?, onDismiss: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition()
  val scale by infiniteTransition.animateFloat(
    initialValue = 0.8f, targetValue = 1f,
    animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
  )

  Column(
    modifier = Modifier.fillMaxSize().background(PinkPrimary),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier.size(160.dp).scale(scale).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Text(
      text = "JOURNEY COMPLETE",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
      color = Color.White
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
      text = "You're safely home.",
      style = MaterialTheme.typography.titleMedium,
      color = Color.White.copy(alpha = 0.9f)
    )

    if (message != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
      )
    }

    Spacer(modifier = Modifier.height(64.dp))

    Button(
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PinkPrimaryDark)
    ) {
      Text("DONE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
  }
}
