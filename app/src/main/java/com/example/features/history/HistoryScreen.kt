package com.example.features.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.TriggerType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  viewModel: HistoryViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "EVIDENCE & LOGS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (uiState.events.isNotEmpty()) {
            IconButton(onClick = { viewModel.clearHistory() }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear History")
            }
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
    if (uiState.events.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = PinkBorderSubtle
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No emergency events recorded.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = TextSecondaryLight
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Text(
            text = "RECENT EVENTS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
            color = TextSecondaryLight,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
          )
        }
        items(uiState.events, key = { it.id }) { event ->
          EventHistoryCard(event = event)
        }
      }
    }
  }
}

@Composable
fun EventHistoryCard(event: EmergencyEvent) {
  val isResolved = event.status == com.example.domain.model.EmergencyStatus.RESOLVED
  val iconColor = if (isResolved) PinkPrimary else SafetyRedPrimary
  val bgColor = if (isResolved) PinkContainer.copy(alpha = 0.3f) else SafetyRedLight.copy(alpha = 0.3f)
  val borderColor = if (isResolved) PinkBorderSubtle else SafetyRedDark.copy(alpha = 0.3f)
  val icon = if (isResolved) Icons.Default.CheckCircle else Icons.Default.Warning

  val timeFormatted = remember(event.timestamp) {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(event.timestamp))
  }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isResolved) "Resolved Emergency" else "Active Emergency",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isResolved) PinkPrimaryDark else SafetyRedDark
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = timeFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
          )
        }
        
        // Export button
        IconButton(onClick = { /* Export PDF */ }, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.Download, contentDescription = "Export Log", tint = PinkPrimary)
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = PinkBorderSubtle)
      Spacer(modifier = Modifier.height(16.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
          Text("Trigger", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = PinkPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = event.triggerType.name.replace("_", " "),
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = PinkPrimaryDark
            )
          }
        }
        
        Column(horizontalAlignment = Alignment.End) {
          Text("Location Logs", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = PinkPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Location tracked",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = PinkPrimaryDark
            )
          }
        }
      }
    }
  }
}
