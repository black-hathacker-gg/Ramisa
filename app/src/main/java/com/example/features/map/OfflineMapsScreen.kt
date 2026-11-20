package com.example.features.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapsScreen(
  onNavigateBack: () -> Unit,
  viewModel: OfflineMapsViewModel = viewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
  var previewId by rememberSaveable { mutableStateOf<String?>(null) }

  Scaffold(topBar = {
    TopAppBar(title = { Text("Offline maps") }, navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }
    })
  }) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Text(
          "Download a city area to view its streets without internet. Routing requires a navigation app.",
          style = MaterialTheme.typography.bodyMedium
        )
      }
      if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
      state.error?.let { message ->
        item {
          Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
          TextButton(onClick = viewModel::refresh) { Text("Refresh") }
        }
      }
      items(state.regions, key = { it.id }) { region ->
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(region.name, style = MaterialTheme.typography.titleMedium)
            Text(region.description, style = MaterialTheme.typography.bodySmall)
            if (region.downloadedBytes > 0) {
              val size = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }
                .format(region.downloadedBytes / 1_000_000.0)
              Text("$size MB of map data", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              when (region.state) {
                DownloadState.NOT_DOWNLOADED, DownloadState.PAUSED -> {
                  TextButton(
                    onClick = { viewModel.startDownload(region.id) },
                    enabled = !state.loading && state.activeRegionId == null
                  ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (region.state == DownloadState.PAUSED) "Resume" else "Download")
                  }
                }
                DownloadState.DOWNLOADING -> {
                  IconButton(onClick = { viewModel.pauseDownload(region.id) }) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause download for " + region.name)
                  }
                  val progress = region.progress
                  val progressModifier = Modifier.weight(1f)
                  if (progress == null) LinearProgressIndicator(progressModifier)
                  else LinearProgressIndicator(progress = { progress }, modifier = progressModifier)
                }
                DownloadState.DOWNLOADED -> TextButton(onClick = { previewId = region.id }) {
                  Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                  Text("View offline area")
                }
                DownloadState.DELETING -> LinearProgressIndicator(Modifier.fillMaxWidth())
              }
              if (region.state == DownloadState.DOWNLOADED || region.state == DownloadState.PAUSED) {
                IconButton(onClick = { deleteId = region.id }) {
                  Icon(Icons.Default.Delete, contentDescription = "Remove download for " + region.name)
                }
              }
            }
          }
        }
      }
    }
  }

  state.regions.firstOrNull { it.id == deleteId }?.let { region ->
    AlertDialog(
      onDismissRequest = { deleteId = null },
      title = { Text("Remove downloaded map?") },
      text = { Text("Remove " + region.name + " from your offline maps.") },
      confirmButton = {
        TextButton(onClick = {
          viewModel.deleteRegion(region.id)
          deleteId = null
        }) { Text("Remove") }
      },
      dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } }
    )
  }

  state.regions.firstOrNull { it.id == previewId }?.let { region ->
    AlertDialog(
      onDismissRequest = { previewId = null },
      title = { Text(region.name) },
      text = {
        LiveSafetyMap(
          points = emptyList(),
          deviceLocation = null,
          showRiskAreas = false,
          onPointClick = {},
          onMapLongPress = {},
          modifier = Modifier.fillMaxWidth().height(320.dp),
          cameraAction = 0L to MapCameraAction.Center(region.bounds.center, 12.5)
        )
      },
      confirmButton = { TextButton(onClick = { previewId = null }) { Text("Close") } }
    )
  }
}
