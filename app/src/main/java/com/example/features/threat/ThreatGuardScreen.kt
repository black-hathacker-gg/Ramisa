package com.example.features.threat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ThreatLevel
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatGuardScreen(
  viewModel: ThreatGuardViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.triggeredAutoSos) {
    if (uiState.triggeredAutoSos) {
      onTriggerSos()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (uiState.isMonitoring) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_radar_scale"
  )

  val threatColor by animateColorAsState(
    targetValue = when (uiState.analysisResult.threatLevel) {
      ThreatLevel.LOW -> SafeGreenPrimary
      ThreatLevel.MEDIUM -> Color(0xFFE65100)
      ThreatLevel.HIGH -> SafetyRedPrimary
      ThreatLevel.CRITICAL -> SafetyRedDark
    },
    label = "threat_color"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "AI Threat Guard (শব্দ ও বিপদ মনিটর)",
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
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Status Bar
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isMonitoring) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (uiState.isMonitoring) SafeGreenPrimary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (uiState.isMonitoring) Icons.Default.Sensors else Icons.Default.MicOff,
                contentDescription = null,
                tint = if (uiState.isMonitoring) SafeGreenPrimary else Color.Gray,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (uiState.isMonitoring) "ACTIVE LISTENING" else "MONITORING OFF",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (uiState.isMonitoring) SafeGreenDark else Color.Gray
              )
              Text(
                text = if (uiState.isMonitoring) "Acoustic sensor processing" else "Tap below to begin",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = { viewModel.toggleMonitoring() },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (uiState.isMonitoring) SafetyRedPrimary else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(if (uiState.isMonitoring) "STOP" else "START")
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Circular Acoustic Radar / Visualizer
      Box(
        modifier = Modifier
          .size(190.dp)
          .testTag("acoustic_radar_box"),
        contentAlignment = Alignment.Center
      ) {
        if (uiState.isMonitoring) {
          Box(
            modifier = Modifier
              .size(180.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(threatColor.copy(alpha = 0.15f))
          )
        }

        Surface(
          modifier = Modifier
            .size(140.dp)
            .clip(CircleShape),
          color = MaterialTheme.colorScheme.surface,
          shape = CircleShape,
          tonalElevation = 4.dp
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .border(3.dp, threatColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = threatColor,
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${uiState.currentDecibels} dB",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = uiState.analysisResult.threatLevel.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = threatColor
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Realtime Audio Waveform Visualizer Bars
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        uiState.audioSamplePoints.forEach { sample ->
          val barHeight = (sample.coerceIn(0.1f, 1f) * 36).dp
          Box(
            modifier = Modifier
              .width(6.dp)
              .height(barHeight)
              .clip(RoundedCornerShape(3.dp))
              .background(
                if (sample > 0.75f) SafetyRedPrimary
                else if (sample > 0.45f) Color(0xFFE65100)
                else MaterialTheme.colorScheme.primary
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Threat Analysis Diagnostic Card
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
            Text(
              text = "Threat Analysis Status",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = threatColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${(uiState.analysisResult.confidenceScore * 100).toInt()}% Confidence",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = threatColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = uiState.analysisResult.recommendedAction,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = uiState.analysisResult.recommendedActionBn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )

          if (uiState.analysisResult.detectedAnomalies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Detected Acoustic Anomalies:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            uiState.analysisResult.detectedAnomalies.forEach { anomaly ->
              Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = SafetyRedPrimary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = anomaly,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = SafetyRedDark
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Auto-SOS Escalation Toggle
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Auto-Trigger SOS on Critical Threat",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Automatically initiates emergency dispatch if scream / acoustic shock signature is confirmed.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Switch(
            checked = uiState.isAutoSosEnabled,
            onCheckedChange = { viewModel.toggleAutoSos(it) }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Test Spike Simulation Button
      OutlinedButton(
        onClick = { viewModel.simulateThreatSpike() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Simulate Critical Distress Spike (Test)")
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
