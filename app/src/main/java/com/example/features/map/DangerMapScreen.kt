package com.example.features.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.location.EmergencyLocationManager
import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerMapScreen(
  viewModel: DangerMapViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbar = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val permissionRequest = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { granted ->
    if (granted.values.any { it }) viewModel.refreshLocation() else viewModel.locationDenied()
  }

  LaunchedEffect(Unit) {
    if (EmergencyLocationManager.hasLocationPermission(context)) {
      viewModel.refreshLocation(recenter = false)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbar) },
    topBar = {
      TopAppBar(
        title = { Text("Safety map") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val location = state.deviceLocation
              if (location != null && viewModel.isFresh(location)) {
                viewModel.openReportDialog(LatLng(location.latitude, location.longitude))
              } else {
                scope.launch { snackbar.showSnackbar("Press and hold the spot on the map to add a report.") }
              }
            },
            modifier = Modifier.testTag("btn_report_unsafe_spot")
          ) {
            Icon(Icons.Default.AddLocation, contentDescription = "Report an unsafe spot")
          }
          TextButton(onClick = onTriggerSos) {
            Text("SOS", color = MaterialTheme.colorScheme.error)
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        OutlinedTextField(
          value = state.searchQuery,
          onValueChange = viewModel::setSearchQuery,
          label = { Text("Search risk pins") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          item {
            FilterChip(
              selected = state.mapLayer == MapLayerType.HEATMAP,
              onClick = { viewModel.setMapLayer(MapLayerType.HEATMAP) },
              label = { Text("Heatmap") }
            )
          }
          item {
            FilterChip(
              selected = state.mapLayer == MapLayerType.RISK_PINS,
              onClick = { viewModel.setMapLayer(MapLayerType.RISK_PINS) },
              label = { Text("Pins") }
            )
          }
          item {
            FilterChip(
              selected = state.mapLayer == MapLayerType.SAFE_CORRIDORS,
              onClick = { viewModel.setMapLayer(MapLayerType.SAFE_CORRIDORS) },
              label = { Text("Bypass advice") }
            )
          }
        }
      }
      item {
        val mapPoints = remember(state.filteredDangerZones, state.selectedZone) {
          state.filteredDangerZones.map { zone ->
            MapPoint(zone.id, LatLng(zone.latitude, zone.longitude), zone.riskLevel.pinColor(), state.selectedZone?.id == zone.id)
          }
        }
        Box(Modifier.fillMaxWidth().height(340.dp).clip(MaterialTheme.shapes.large)) {
          LiveSafetyMap(
            points = mapPoints,
            deviceLocation = state.deviceLocation?.let { LatLng(it.latitude, it.longitude) },
            showRiskAreas = state.mapLayer == MapLayerType.HEATMAP,
            onPointClick = { id -> state.filteredDangerZones.firstOrNull { it.id == id }?.let { zone -> viewModel.selectZone(zone) } },
            onMapLongPress = viewModel::openReportDialog,
            modifier = Modifier.fillMaxSize().testTag("canvas_danger_heatmap"),
            cameraAction = state.cameraAction
          )
          Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 2.dp
          ) {
            Column {
              IconButton(onClick = { viewModel.zoom(1.0) }, modifier = Modifier.testTag("btn_map_zoom_in")) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
              }
              IconButton(onClick = { viewModel.zoom(-1.0) }, modifier = Modifier.testTag("btn_map_zoom_out")) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
              }
              IconButton(
                onClick = {
                  if (EmergencyLocationManager.hasLocationPermission(context)) viewModel.refreshLocation()
                  else permissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                },
                enabled = !state.centeringLocation,
                modifier = Modifier.testTag("btn_map_center_me")
              ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My location")
              }
            }
          }
        }
      }
      item {
        Text(
          "The map uses real streets. Risk pins are examples or your saved reports. Press and hold a spot to add a report.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.locationMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        state.nearestLocalReport?.let { zone ->
          val fix = state.deviceLocation
          if (fix != null && viewModel.isFresh(fix)) {
            Text(
              "Near your saved report at " + zone.name + " (" + viewModel.distanceLabel(fix, zone) + ")",
              modifier = Modifier.testTag("banner_proximity_alert"),
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.titleSmall
            )
          }
        }
      }
      if (state.mapLayer == MapLayerType.SAFE_CORRIDORS) {
        item {
          Text(
            "Select an example pin to view its bypass suggestion. Verify current conditions before using it.",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
      state.selectedZone?.let { zone ->
        item("selected_zone") {
          ZoneDetails(
            zone,
            state.deviceLocation?.takeIf(viewModel::isFresh)?.let { viewModel.distanceLabel(it, zone) },
            onDirections = {
              if (!openMapDirections(context, zone.latitude, zone.longitude, zone.name)) {
                scope.launch { snackbar.showSnackbar("No map or browser app is available.") }
              }
            },
            onCall = {
              if (!openDialer(context, zone.policeContact)) {
                scope.launch { snackbar.showSnackbar("No dialer app is available.") }
              }
            }
          )
        }
      }
      item {
        Text("Risk pins (" + state.filteredDangerZones.size + ")", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          item {
            FilterChip(
              selected = state.selectedRiskFilter == null && state.selectedCategoryFilter == null,
              onClick = {
                viewModel.setRiskFilter(null)
                viewModel.setCategoryFilter(null)
              },
              label = { Text("All") }
            )
          }
          items(RiskLevel.entries) { risk ->
            FilterChip(
              selected = state.selectedRiskFilter == risk,
              onClick = { viewModel.setRiskFilter(risk) },
              label = { Text(risk.label()) }
            )
          }
          items(DangerCategory.entries) { category ->
            FilterChip(
              selected = state.selectedCategoryFilter == category,
              onClick = { viewModel.setCategoryFilter(category) },
              label = { Text(category.label()) }
            )
          }
        }
      }
      if (state.filteredDangerZones.isEmpty()) {
        item { Text("No pins match those filters.", style = MaterialTheme.typography.bodyMedium) }
      }
      items(state.filteredDangerZones, key = { it.id }) { zone ->
        OutlinedCard(
          onClick = { viewModel.selectZone(zone, recenter = true) },
          modifier = Modifier.fillMaxWidth().testTag("item_danger_zone_" + zone.id)
        ) {
          ListItem(
            headlineContent = { Text(zone.name) },
            supportingContent = {
              Text(if (zone.isLocalReport) "Saved on this device" else "Example pin · " + zone.areaName)
            }
          )
        }
      }
    }
  }

  state.reportPoint?.let { coordinates ->
    var name by rememberSaveable(coordinates) { mutableStateOf("") }
    var area by rememberSaveable(coordinates) { mutableStateOf("") }
    var reason by rememberSaveable(coordinates) { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = viewModel::closeReportDialog,
      title = { Text(if (state.reportSubmissionSuccess) "Report saved" else "Report an unsafe spot") },
      text = {
        if (state.reportSubmissionSuccess) {
          Text("Your report appears on the map on this device.")
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("The report is stored on this device.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(name, { name = it }, label = { Text("Spot name") }, singleLine = true)
            OutlinedTextField(area, { area = it }, label = { Text("Area or thana") }, singleLine = true)
            OutlinedTextField(reason, { reason = it }, label = { Text("What happened?") })
            state.reportError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
          }
        }
      },
      confirmButton = {
        if (state.reportSubmissionSuccess) {
          Button(onClick = viewModel::closeReportDialog) { Text("Done") }
        } else {
          Button(
            onClick = { viewModel.submitUnsafeSpot(name, area, reason) },
            enabled = !state.savingReport && name.isNotBlank() && reason.isNotBlank()
          ) { Text(if (state.savingReport) "Saving…" else "Save report") }
        }
      },
      dismissButton = {
        if (!state.reportSubmissionSuccess) {
          TextButton(onClick = viewModel::closeReportDialog, enabled = !state.savingReport) { Text("Cancel") }
        }
      }
    )
  }
}

@Composable
private fun ZoneDetails(zone: DangerZone, distance: String?, onDirections: () -> Unit, onCall: () -> Unit) {
  OutlinedCard(modifier = Modifier.fillMaxWidth().testTag("card_selected_danger_zone")) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(if (zone.isLocalReport) "Your report" else "Example incident · " + zone.riskLevel.label(),
        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
      Text(zone.name, style = MaterialTheme.typography.titleMedium)
      if (zone.areaName.isNotBlank()) Text(zone.areaName)
      distance?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
      Text(zone.reportedIncidentsSummary, style = MaterialTheme.typography.bodyMedium)
      if (!zone.isLocalReport) {
        Text("Example counts · " + zone.incidentStats, style = MaterialTheme.typography.bodySmall)
        Text("Example time · " + zone.peakVulnerableHours, style = MaterialTheme.typography.bodySmall)
      }
      if (zone.safeAlternativeRoute.isNotBlank()) {
        Text("Bypass suggestion", style = MaterialTheme.typography.titleSmall)
        Text(zone.safeAlternativeRoute, style = MaterialTheme.typography.bodySmall)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onDirections, modifier = Modifier.testTag("btn_directions_danger_zone")) {
          Icon(Icons.Default.Directions, contentDescription = null)
          Text("Directions", modifier = Modifier.padding(start = 4.dp))
        }
        if (zone.policeContact.isNotBlank()) {
          TextButton(onClick = onCall, modifier = Modifier.testTag("btn_call_police_danger_zone")) {
            Icon(Icons.Default.Call, contentDescription = null)
            Text("Station contact", modifier = Modifier.padding(start = 4.dp))
          }
        }
      }
    }
  }
}

private fun RiskLevel.pinColor(): String = when (this) {
  RiskLevel.CRITICAL_DANGER -> "#B91C1C"
  RiskLevel.HIGH_RISK -> "#D05B0C"
  RiskLevel.MODERATE_CAUTION -> "#9C6900"
}

private fun RiskLevel.label(): String = when (this) {
  RiskLevel.CRITICAL_DANGER -> "Critical"
  RiskLevel.HIGH_RISK -> "High risk"
  RiskLevel.MODERATE_CAUTION -> "Caution"
}

private fun DangerCategory.label(): String = when (this) {
  DangerCategory.SEXUAL_HARASSMENT_HOTSPOT -> "Harassment"
  DangerCategory.ISOLATED_DARK_CORRIDOR -> "Dark corridors"
  DangerCategory.HIGH_CRIME_TRANSIT_HUB -> "Transit hubs"
  DangerCategory.UNMONITORED_PARK_ALLEY -> "Parks and alleys"
}

internal fun openMapDirections(context: Context, latitude: Double, longitude: Double, name: String): Boolean {
  val point = "$latitude,$longitude"
  val map = Uri.parse("geo:$point?q=" + Uri.encode("$point ($name)"))
  val directions = Uri.parse("https://www.google.com/maps/dir/")
    .buildUpon().appendQueryParameter("api", "1").appendQueryParameter("destination", point).build()
  return launchViewIntent(context, Intent(Intent.ACTION_VIEW, map)) ||
    launchViewIntent(context, Intent(Intent.ACTION_VIEW, directions))
}

internal fun openDialer(context: Context, phone: String): Boolean =
  launchViewIntent(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)))

private fun launchViewIntent(context: Context, intent: Intent): Boolean = try {
  context.startActivity(intent)
  true
} catch (_: ActivityNotFoundException) {
  false
} catch (_: SecurityException) {
  false
}
