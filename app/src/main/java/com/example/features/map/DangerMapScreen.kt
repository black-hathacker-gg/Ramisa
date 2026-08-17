package com.example.features.map

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerMapScreen(
  viewModel: DangerMapViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Danger & Incident Heatmap",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "বিপদজনক এলাকা ও নিরাপত্তা রাডার",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.openReportDialog() },
            modifier = Modifier.testTag("btn_report_unsafe_spot")
          ) {
            Icon(
              imageVector = Icons.Default.AddLocation,
              contentDescription = "Report Unsafe Spot",
              tint = SafetyRedPrimary
            )
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
        .verticalScroll(scrollState)
    ) {
      // PROXIMITY ALERT BANNER IF NEAR DANGER ZONE
      if (uiState.isProximityAlertActive && uiState.nearestDangerZone != null) {
        val zone = uiState.nearestDangerZone!!
        Surface(
          color = Color(0xFFFFEBEE),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("banner_proximity_alert"),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SafetyRedPrimary.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SafetyRedPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "CAUTION: Near ${zone.areaName} (${uiState.distanceToNearestKm} km)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SafetyRedDark
              )
              Text(
                text = "High sexual harassment & assault incidence recorded here. Stay vigilant.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF5D101D)
              )
            }
          }
        }
      }

      // MAP CONTROLS & FILTER ROW
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          FilterChip(
            selected = uiState.selectedRiskFilter == null,
            onClick = { viewModel.setRiskFilter(null) },
            label = { Text("All Risk Zones (${uiState.allDangerZones.size})", fontSize = 11.sp) }
          )
        }
        item {
          FilterChip(
            selected = uiState.selectedRiskFilter == RiskLevel.CRITICAL_DANGER,
            onClick = { viewModel.setRiskFilter(RiskLevel.CRITICAL_DANGER) },
            label = { Text("🔴 Critical Danger", fontSize = 11.sp) }
          )
        }
        item {
          FilterChip(
            selected = uiState.selectedRiskFilter == RiskLevel.HIGH_RISK,
            onClick = { viewModel.setRiskFilter(RiskLevel.HIGH_RISK) },
            label = { Text("🟠 High Risk", fontSize = 11.sp) }
          )
        }
        item {
          FilterChip(
            selected = uiState.selectedCategoryFilter == DangerCategory.SEXUAL_HARASSMENT_HOTSPOT,
            onClick = { viewModel.setCategoryFilter(DangerCategory.SEXUAL_HARASSMENT_HOTSPOT) },
            label = { Text("Harassment Hotspots", fontSize = 11.sp) }
          )
        }
        item {
          FilterChip(
            selected = uiState.selectedCategoryFilter == DangerCategory.ISOLATED_DARK_CORRIDOR,
            onClick = { viewModel.setCategoryFilter(DangerCategory.ISOLATED_DARK_CORRIDOR) },
            label = { Text("Dark Corridors", fontSize = 11.sp) }
          )
        }
      }

      // MAIN INTERACTIVE DANGER CANVAS MAP
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
          .padding(horizontal = 16.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF0F172A)) // Night map navy canvas
          .testTag("canvas_danger_heatmap")
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
        val radarRadius by infiniteTransition.animateFloat(
          initialValue = 10f,
          targetValue = 65f,
          animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "radar_pulse"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
          initialValue = 0.8f,
          targetValue = 0.0f,
          animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "pulse_alpha"
        )

        // Custom Rendered Vector Map Canvas
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(uiState.filteredDangerZones) {
              detectTapGestures { offset ->
                // Calculate closest danger zone click
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                var closest: DangerZone? = null
                var minDist = Float.MAX_VALUE

                uiState.filteredDangerZones.forEach { zone ->
                  val pos = mapGeoToCanvas(zone.latitude, zone.longitude, width, height, uiState.zoomLevel)
                  val dist = kotlin.math.hypot(offset.x - pos.x, offset.y - pos.y)
                  if (dist < 40f && dist < minDist) {
                    minDist = dist
                    closest = zone
                  }
                }
                closest?.let { viewModel.selectZone(it) }
              }
            }
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height

          // Draw Simulated City Road Network Lines
          drawCityRoadGrid(canvasWidth, canvasHeight)

          // Draw Safe Corridors (Green connecting paths with police checkpoints)
          drawSafeCorridors(canvasWidth, canvasHeight, uiState.zoomLevel)

          // Draw Danger Heatmap Radiuses and Pins
          uiState.filteredDangerZones.forEach { zone ->
            val zonePos = mapGeoToCanvas(zone.latitude, zone.longitude, canvasWidth, canvasHeight, uiState.zoomLevel)
            val isSelected = uiState.selectedZone?.id == zone.id

            // Danger Heat Bubble Circle
            val heatColor = when (zone.riskLevel) {
              RiskLevel.CRITICAL_DANGER -> Color(0xFFEF4444)
              RiskLevel.HIGH_RISK -> Color(0xFFF97316)
              RiskLevel.MODERATE_CAUTION -> Color(0xFFEAB308)
            }

            // Outer Heat Diffusion
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(
                  heatColor.copy(alpha = if (isSelected) 0.55f else 0.35f),
                  heatColor.copy(alpha = 0.10f),
                  Color.Transparent
                ),
                center = zonePos,
                radius = if (isSelected) 55f * uiState.zoomLevel else 40f * uiState.zoomLevel
              ),
              center = zonePos,
              radius = if (isSelected) 55f * uiState.zoomLevel else 40f * uiState.zoomLevel
            )

            // Danger Pin Core
            drawCircle(
              color = Color.White,
              radius = if (isSelected) 10f else 7f,
              center = zonePos
            )
            drawCircle(
              color = heatColor,
              radius = if (isSelected) 8f else 5f,
              center = zonePos
            )

            // Selection Halo
            if (isSelected) {
              drawCircle(
                color = Color.White,
                radius = 18f,
                center = zonePos,
                style = Stroke(width = 2.5f)
              )
            }
          }

          // Draw User's Current GPS Location (Dhanmondi / Dhaka Center)
          val userPos = mapGeoToCanvas(uiState.userLatitude, uiState.userLongitude, canvasWidth, canvasHeight, uiState.zoomLevel)

          // Pulsing safety perimeter radar
          drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
            radius = radarRadius,
            center = userPos,
            style = Stroke(width = 2f)
          )

          // User Core Dot
          drawCircle(
            color = Color.White,
            radius = 9f,
            center = userPos
          )
          drawCircle(
            color = Color(0xFF00B0FF),
            radius = 6f,
            center = userPos
          )
        }

        // Overlay Map Legend & Floating Actions
        Column(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Zoom In
          Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            modifier = Modifier
              .size(36.dp)
              .clickable { viewModel.zoomIn() }
              .testTag("btn_map_zoom_in")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }

          // Zoom Out
          Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            modifier = Modifier
              .size(36.dp)
              .clickable { viewModel.zoomOut() }
              .testTag("btn_map_zoom_out")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }

          // Center on User
          Surface(
            shape = CircleShape,
            color = Color(0xFF0284C7).copy(alpha = 0.95f),
            modifier = Modifier
              .size(36.dp)
              .clickable { viewModel.centerOnUser() }
              .testTag("btn_map_center_me")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }

        // Bottom Map Legend Strip
        Surface(
          color = Color(0xFF0F172A).copy(alpha = 0.90f),
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(8.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Critical Assault/Harassment", color = Color(0xFFCBD5E1), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Safe Corridors", color = Color(0xFFCBD5E1), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Your GPS", color = Color(0xFFCBD5E1), fontSize = 10.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // SELECTED DANGER ZONE DETAILED CARD
      uiState.selectedZone?.let { zone ->
        val dist = viewModel.calculateDistanceKm(uiState.userLatitude, uiState.userLongitude, zone.latitude, zone.longitude)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("card_selected_danger_zone"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header & Risk Pill
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (zone.riskLevel) {
                  RiskLevel.CRITICAL_DANGER -> Color(0xFFFFCDD2)
                  RiskLevel.HIGH_RISK -> Color(0xFFFFE0B2)
                  RiskLevel.MODERATE_CAUTION -> Color(0xFFFFF9C4)
                }
              ) {
                Text(
                  text = when (zone.riskLevel) {
                    RiskLevel.CRITICAL_DANGER -> "🚨 CRITICAL DANGER ZONE"
                    RiskLevel.HIGH_RISK -> "⚠️ HIGH RISK AREA"
                    RiskLevel.MODERATE_CAUTION -> "🟡 MODERATE CAUTION"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = when (zone.riskLevel) {
                      RiskLevel.CRITICAL_DANGER -> SafetyRedDark
                      RiskLevel.HIGH_RISK -> Color(0xFFE65100)
                      RiskLevel.MODERATE_CAUTION -> Color(0xFFF57F17)
                    },
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }

              Text(
                text = "$dist km from you",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = zone.name,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${zone.areaName}, ${zone.district}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Incident History & Data Summary
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "Documented Incidents & Media Reports:",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = SafetyRedDark
                )
                Text(
                  text = zone.reportedIncidentsSummary,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "📊 ${zone.incidentStats}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "⏰ Peak: ${zone.peakVulnerableHours}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFC2410C)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Safe Alternative Route
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFE8F5E9),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
              ) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = SafeGreenDark,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Recommended Safe Bypass Corridor:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SafeGreenDark
                  )
                  Text(
                    text = zone.safeAlternativeRoute,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF1B5E20)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Open Google Maps & Call Area Police
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val gmmIntentUri = Uri.parse("geo:${zone.latitude},${zone.longitude}?q=${Uri.encode(zone.name)}")
                  val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                  try {
                    context.startActivity(mapIntent)
                  } catch (_: Exception) {}
                },
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("btn_directions_danger_zone"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BYPASS ROUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${zone.policeContact}"))
                  try {
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("btn_call_police_danger_zone"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("POLICE THANA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // LIST OF ALL CRITICAL INCIDENT LOCATIONS
      Text(
        text = "Documented Vulnerable Hotspots (${uiState.filteredDangerZones.size})",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        uiState.filteredDangerZones.forEach { zone ->
          val isSelected = uiState.selectedZone?.id == zone.id
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.selectZone(zone) }
              .testTag("item_danger_zone_${zone.id}"),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 1.5.dp else 0.5.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(
                    when (zone.riskLevel) {
                      RiskLevel.CRITICAL_DANGER -> Color(0xFFFFCDD2)
                      RiskLevel.HIGH_RISK -> Color(0xFFFFE0B2)
                      RiskLevel.MODERATE_CAUTION -> Color(0xFFFFF9C4)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (zone.riskLevel) {
                    RiskLevel.CRITICAL_DANGER -> Icons.Default.ReportProblem
                    RiskLevel.HIGH_RISK -> Icons.Default.Warning
                    RiskLevel.MODERATE_CAUTION -> Icons.Default.Info
                  },
                  contentDescription = null,
                  tint = when (zone.riskLevel) {
                    RiskLevel.CRITICAL_DANGER -> SafetyRedDark
                    RiskLevel.HIGH_RISK -> Color(0xFFE65100)
                    RiskLevel.MODERATE_CAUTION -> Color(0xFFF57F17)
                  },
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = zone.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${zone.areaName} • ${zone.incidentStats}",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = "View on Map",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // REPORT UNSAFE LOCATION DIALOG
  if (uiState.showReportDialog) {
    ReportUnsafeSpotDialog(
      isSuccess = uiState.reportSubmissionSuccess,
      onDismiss = { viewModel.closeReportDialog() },
      onSubmit = { name, area, reason ->
        viewModel.submitUnsafeSpot(name, area, reason)
      }
    )
  }
}

@Composable
private fun ReportUnsafeSpotDialog(
  isSuccess: Boolean,
  onDismiss: () -> Unit,
  onSubmit: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var area by remember { mutableStateOf("") }
  var reason by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isSuccess) "Report Submitted" else "Flag Unsafe Location / Harassment Spot",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      if (isSuccess) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreenPrimary, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Thank you. Your report has been added to the safety community radar to warn fellow women and students in Dhaka.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Help other women by flagging unlit alleys, eve-teasing spots, or isolated corners.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Spot Name (e.g., Road 27 Dark Underpass)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Area / Thana (e.g., Mirpur, Dhanmondi)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Describe Hazard (e.g., Unlit, groups stalking)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      if (isSuccess) {
        Button(onClick = onDismiss) { Text("Done") }
      } else {
        Button(
          onClick = {
            if (name.isNotBlank() && reason.isNotBlank()) {
              onSubmit(name, area.ifBlank { "Dhaka" }, reason)
            }
          },
          enabled = name.isNotBlank() && reason.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
        ) {
          Text("Submit Hazard Report")
        }
      }
    },
    dismissButton = {
      if (!isSuccess) {
        OutlinedButton(onClick = onDismiss) { Text("Cancel") }
      }
    }
  )
}

// Canvas Helpers for Road Network & Safe Corridors
private fun DrawScope.drawCityRoadGrid(width: Float, height: Float) {
  val roadColor = Color(0xFF334155)
  val minorRoadColor = Color(0xFF1E293B)

  // Minor grid lines
  for (i in 1..8) {
    val y = height * (i / 9f)
    drawLine(color = minorRoadColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 2f)
  }
  for (i in 1..8) {
    val x = width * (i / 9f)
    drawLine(color = minorRoadColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 2f)
  }

  // Major Dhaka Arterial Roads (Mirpur Rd, Airport Rd, Hatirjheel loop)
  // Mirpur Road North-South Spine
  drawLine(
    color = roadColor,
    start = Offset(width * 0.35f, 0f),
    end = Offset(width * 0.40f, height),
    strokeWidth = 5f
  )
  // Kazi Nazrul Islam Avenue & Shahbagh Corridor
  drawLine(
    color = roadColor,
    start = Offset(width * 0.55f, 0f),
    end = Offset(width * 0.50f, height),
    strokeWidth = 6f
  )
  // East-West Connecting Highway (Bijoy Sarani / Panthapath)
  drawLine(
    color = roadColor,
    start = Offset(0f, height * 0.45f),
    end = Offset(width, height * 0.42f),
    strokeWidth = 5f
  )
  // South Connecting Highway (Shahbagh - Jatrabari)
  drawLine(
    color = roadColor,
    start = Offset(0f, height * 0.75f),
    end = Offset(width, height * 0.80f),
    strokeWidth = 5f
  )

  // Hatirjheel Circular Water Ring
  drawCircle(
    color = Color(0xFF0369A1).copy(alpha = 0.35f),
    radius = 35f,
    center = Offset(width * 0.65f, height * 0.45f),
    style = Stroke(width = 4f)
  )
}

private fun DrawScope.drawSafeCorridors(width: Float, height: Float, zoom: Float) {
  val safeGreen = Color(0xFF10B981)
  val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)

  // Safe Police Corridor 1: Shahbagh to Dhanmondi via Science Lab
  drawLine(
    color = safeGreen.copy(alpha = 0.7f),
    start = Offset(width * 0.52f, height * 0.60f),
    end = Offset(width * 0.40f, height * 0.55f),
    strokeWidth = 4f,
    pathEffect = dashedEffect
  )

  // Safe Police Corridor 2: Farmgate to TSC via Bangla Motor
  drawLine(
    color = safeGreen.copy(alpha = 0.7f),
    start = Offset(width * 0.48f, height * 0.38f),
    end = Offset(width * 0.52f, height * 0.62f),
    strokeWidth = 4f,
    pathEffect = dashedEffect
  )

  // Safe Corridor 3: Gulshan Avenue Main Strip
  drawLine(
    color = safeGreen.copy(alpha = 0.7f),
    start = Offset(width * 0.70f, height * 0.20f),
    end = Offset(width * 0.68f, height * 0.40f),
    strokeWidth = 4f,
    pathEffect = dashedEffect
  )
}

// Convert Geo coordinates around Dhaka (23.70 to 23.88 Lat, 90.33 to 90.45 Lng) to Canvas 2D space
private fun mapGeoToCanvas(lat: Double, lng: Double, width: Float, height: Float, zoom: Float): Offset {
  val minLat = 23.7000
  val maxLat = 23.8800
  val minLng = 90.3300
  val maxLng = 90.4500

  val normX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.0, 1.0).toFloat()
  // Latitude goes bottom-to-top, so invert Y
  val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).coerceIn(0.0, 1.0)).toFloat()

  val centerX = width / 2f
  val centerY = height / 2f

  val rawX = normX * width
  val rawY = normY * height

  val zoomedX = centerX + (rawX - centerX) * zoom
  val zoomedY = centerY + (rawY - centerY) * zoom

  return Offset(zoomedX.coerceIn(20f, width - 20f), zoomedY.coerceIn(20f, height - 20f))
}
