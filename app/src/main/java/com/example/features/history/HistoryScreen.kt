package com.example.features.history

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyEvent
import com.example.domain.model.TriggerType
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedPrimary
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
            text = "Emergency History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No Safety Logs",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "All emergency activations & safety checks will be logged here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(uiState.events, key = { it.id }) { event ->
          HistoryEventCard(event = event)
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun HistoryEventCard(event: EmergencyEvent) {
  val dateFormatted = remember(event.timestamp) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    sdf.format(Date(event.timestamp))
  }

  val isTest = event.triggerType == TriggerType.TEST_SOS
  val badgeColor = if (isTest) MaterialTheme.colorScheme.primaryContainer else SafeGreenContainer
  val textColor = if (isTest) MaterialTheme.colorScheme.primary else SafeGreenDark

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("history_item_${event.id}"),
    shape = RoundedCornerShape(14.dp),
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
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeColor
        ) {
          Text(
            text = when (event.triggerType) {
              TriggerType.SOS_BUTTON -> "SOS TRIGGERED"
              TriggerType.TEST_SOS -> "TEST SOS MODE"
              TriggerType.SAFETY_CHECK_TIMEOUT -> "SAFETY CHECK"
              TriggerType.JOURNEY_DEVIATION -> "JOURNEY ANOMALY"
              TriggerType.WEARABLE -> "WEARABLE TRIGGER"
            },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Text(
          text = dateFormatted,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = event.locationLabel,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (event.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = event.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
