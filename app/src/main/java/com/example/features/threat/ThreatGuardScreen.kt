package com.example.features.threat

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.domain.model.AudioInputSource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatGuardScreen(
  viewModel: ThreatGuardViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) viewModel.startMonitoring() else viewModel.onMicrophonePermissionDenied()
  }

  DisposableEffect(lifecycle, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP) viewModel.stopMonitoring("Paused because this screen is no longer active. Tap Start to resume.")
    }
    lifecycle.addObserver(observer)
    onDispose {
      lifecycle.removeObserver(observer)
      viewModel.stopMonitoring()
    }
  }

  LaunchedEffect(uiState.triggeredAutoSos) {
    if (uiState.triggeredAutoSos && viewModel.consumeSosRequest()) onTriggerSos()
  }

  val pendingSource = uiState.pendingConfirmationSource
  if (pendingSource != null) {
    val livePrompt = pendingSource == AudioInputSource.LIVE_MICROPHONE
    AlertDialog(
      onDismissRequest = viewModel::cancelConfirmation,
      title = { Text(if (livePrompt) "Possible scream — do you need help?" else "Manual prompt preview") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(if (livePrompt)
            "Two successive audio windows resembled screaming. This model cannot verify danger. Open your SOS flow only if you need help."
          else "This is an interface preview. No sound was classified and this preview cannot send an alert.")
          Text("Closes in ${uiState.confirmationSecondsRemaining}s. Nothing sends automatically.")
        }
      },
      confirmButton = {
        TextButton(onClick = viewModel::confirmSos, modifier = Modifier.testTag("confirm_acoustic_sos")) {
          Text(if (livePrompt) "Open SOS" else "Close preview")
        }
      },
      dismissButton = {
        TextButton(onClick = viewModel::cancelConfirmation, modifier = Modifier.testTag("cancel_acoustic_sos")) { Text("I'm safe / Cancel") }
      }
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("AI sound monitor", fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
      )
    }
  ) { contentPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState()).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("শব্দ পর্যবেক্ষণ", style = MaterialTheme.typography.titleMedium)
      Text("On-device YAMNet recognises sound categories. It cannot establish that you are safe or in danger, and it does not recognise spoken SOS phrases.",
        style = MaterialTheme.typography.bodyMedium)

      Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(if (uiState.isMonitoring) Icons.Default.Mic else Icons.Default.MicOff, contentDescription = null)
            Text(when {
              uiState.isStarting -> "Starting model…"
              uiState.isMonitoring -> "Microphone active"
              uiState.isReplaying -> "Recorded sample replay"
              else -> "Microphone off"
            }, fontWeight = FontWeight.Bold)
          }
          Text(uiState.statusMessage, style = MaterialTheme.typography.bodySmall)
          if (uiState.isStarting || uiState.isReplaying) CircularProgressIndicator()
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              enabled = !uiState.isStarting && !uiState.isReplaying,
              modifier = Modifier.testTag("toggle_audio_monitor"),
              onClick = {
                if (uiState.isMonitoring) viewModel.stopMonitoring()
                else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                  viewModel.startMonitoring()
                } else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
              }
            ) { Text(if (uiState.isMonitoring) "Stop" else "Start microphone") }
            if (uiState.isStarting || uiState.isReplaying) {
              TextButton(onClick = { viewModel.stopMonitoring() }) { Text("Cancel") }
            }
          }
          Text("Monitoring pauses when you leave this screen or lock the phone. Audio is not saved or uploaded.",
            style = MaterialTheme.typography.bodySmall)
        }
      }

      uiState.errorMessage?.let { error ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
          Text(error, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
        }
      }

      Card(modifier = Modifier.fillMaxWidth().testTag("audio_analysis_result")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Latest acoustic result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(when (uiState.analysisResult.source) {
            AudioInputSource.LIVE_MICROPHONE -> "Source: live microphone"
            AudioInputSource.PRERECORDED_REPLAY -> "Source: bundled recording — alerts disabled"
            AudioInputSource.MANUAL_PREVIEW -> "Source: manual preview — alerts disabled"
            AudioInputSource.NONE -> "No audio result yet"
          }, style = MaterialTheme.typography.labelMedium)
          Text(uiState.analysisResult.recommendedAction)
          val score = uiState.analysisResult.screamModelScore
          if (score != null) {
            Text(String.format(Locale.US, "Screaming model score: %.3f", score), fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("scream_model_score"))
            Text("A model score is not a calibrated probability of danger.", style = MaterialTheme.typography.bodySmall)
          }
          uiState.analysisResult.rmsDbfs?.let {
            Text(String.format(Locale.US, "Recorded RMS level: %.1f dBFS", it))
            Text("Relative digital amplitude, not calibrated environmental decibels.", style = MaterialTheme.typography.bodySmall)
          }
          Row(
            Modifier.fillMaxWidth().height(48.dp).testTag("acoustic_waveform"),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
          ) {
            uiState.audioSamplePoints.forEach { amplitude ->
              Box(Modifier.width(5.dp).height((1 + amplitude.coerceIn(0f, 1f) * 46).dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)))
            }
          }
          if (uiState.analysisResult.categories.isNotEmpty()) {
            HorizontalDivider()
            Text("Top sound categories", fontWeight = FontWeight.Bold)
            uiState.analysisResult.categories.forEach { category ->
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.label, modifier = Modifier.weight(1f))
                Text(String.format(Locale.US, "%.3f", category.score))
              }
            }
          }
          uiState.inferenceTimeMs?.let { Text("Last model run: ${it}ms", style = MaterialTheme.typography.labelSmall) }
        }
      }

      Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Offer an SOS confirmation", fontWeight = FontWeight.Bold)
            Text("Off by default. Repeated scream candidates can offer a prompt. Sending still requires your explicit confirmation.",
              style = MaterialTheme.typography.bodySmall)
          }
          Switch(uiState.isAutoSosEnabled, viewModel::toggleAutoSos, modifier = Modifier.testTag("switch_acoustic_sos_prompt"))
        }
      }

      OutlinedButton(onClick = viewModel::previewConfirmation, modifier = Modifier.fillMaxWidth().testTag("preview_audio_prompt")) {
        Text("Preview confirmation — no alerts")
      }
      if (BuildConfig.DEBUG) {
        OutlinedButton(onClick = viewModel::replayBundledSpeech, enabled = !uiState.isReplaying,
          modifier = Modifier.fillMaxWidth().testTag("replay_audio_sample")) {
          Text("Replay speech sample — no alerts")
        }
      }
      Spacer(Modifier.height(8.dp))
    }
  }
}
