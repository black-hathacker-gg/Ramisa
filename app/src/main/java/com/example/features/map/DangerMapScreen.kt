package com.example.features.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import com.example.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerMapScreen(
  viewModel: DangerMapViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("MAP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp))
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
      // Background Map Canvas
      Canvas(modifier = Modifier.fillMaxSize().background(PinkContainer.copy(alpha = 0.2f))) {
        drawCityRoadGrid(size.width, size.height)
      }

      // Main Content Overlay
      Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.White,
          shadowElevation = 4.dp
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
              value = uiState.searchQuery,
              onValueChange = viewModel::setSearchQuery,
              placeholder = { Text("Search safe locations...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PinkPrimary) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinkPrimary,
                unfocusedBorderColor = PinkBorderSubtle,
                cursorColor = PinkPrimary
              ),
              singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.foundation.lazy.LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val filters = listOf("Safe Places", "Police", "Hospitals", "Trusted Contacts")
              items(filters.size) { index ->
                val label = filters[index]
                FilterChip(
                  selected = false,
                  onClick = { /* Apply visual filter */ },
                  label = { Text(label, fontWeight = FontWeight.SemiBold, color = PinkPrimaryDark) },
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = PinkContainer.copy(alpha = 0.5f),
                    selectedContainerColor = PinkPrimary,
                    selectedLabelColor = Color.White
                  ),
                  border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = PinkBorderSubtle
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Map Center Marker Focus
        Box(
          modifier = Modifier
            .align(Alignment.End)
            .padding(16.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, PinkBorderSubtle, CircleShape)
            .clickable { viewModel.centerOnUser() }
            .padding(12.dp)
        ) {
          Icon(Icons.Default.MyLocation, contentDescription = "Center map", tint = PinkPrimary)
        }

        // Bottom Sheet: NEARBY
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
          color = SurfaceCardLight,
          shadowElevation = 16.dp,
          border = BorderStroke(1.dp, PinkBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(24.dp)) {
            Box(
              modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PinkBorderSubtle)
                .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
              text = "NEARBY SAFE PLACES",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
              color = PinkPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            NearbySafePlaceItem(name = "Ramisa Verified Safe Place", distance = "0.5 km", icon = Icons.Default.VerifiedUser)
            Spacer(modifier = Modifier.height(12.dp))
            NearbySafePlaceItem(name = "Mirpur Police Station", distance = "1.2 km", icon = Icons.Default.LocalPolice)
            Spacer(modifier = Modifier.height(12.dp))
            NearbySafePlaceItem(name = "Square Hospital", distance = "2.1 km", icon = Icons.Default.LocalHospital)
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
              onClick = { /* Navigate */ },
              modifier = Modifier.fillMaxWidth().height(56.dp),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
            ) {
              Icon(Icons.Default.Navigation, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("START NAVIGATION", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }
  }
}

@Composable
fun NearbySafePlaceItem(name: String, distance: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(PinkContainer.copy(alpha = 0.3f))
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(PinkPrimary.copy(alpha = 0.1f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = PinkPrimary)
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = PinkPrimaryDark)
      Text(text = distance, style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
    }
    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(16.dp))
  }
}

// Canvas Helpers for Road Network (Pink Theme)
private fun DrawScope.drawCityRoadGrid(width: Float, height: Float) {
  val minorRoadColor = Color(0xFFFDE7EF) // Light blush grid
  val arterialColor = Color(0xFFF8BBD0)

  for (i in 1..8) {
    val y = height * (i / 9f)
    drawLine(color = minorRoadColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 2f)
  }
  for (i in 1..8) {
    val x = width * (i / 9f)
    drawLine(color = minorRoadColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 2f)
  }

  drawLine(
    color = arterialColor,
    start = Offset(width * 0.35f, 0f),
    end = Offset(width * 0.40f, height),
    strokeWidth = 6f
  )
  drawLine(
    color = arterialColor,
    start = Offset(width * 0.55f, 0f),
    end = Offset(width * 0.50f, height),
    strokeWidth = 8f
  )
  
  // Plot some pink safety markers directly on the canvas for effect
  val markerColor = Color(0xFFE91E63) // PinkPrimary
  drawCircle(color = markerColor, radius = 24f, center = Offset(width * 0.45f, height * 0.45f))
  drawCircle(color = Color.White, radius = 12f, center = Offset(width * 0.45f, height * 0.45f))

  drawCircle(color = markerColor.copy(alpha = 0.7f), radius = 18f, center = Offset(width * 0.65f, height * 0.25f))
  drawCircle(color = Color.White, radius = 8f, center = Offset(width * 0.65f, height * 0.25f))
}
