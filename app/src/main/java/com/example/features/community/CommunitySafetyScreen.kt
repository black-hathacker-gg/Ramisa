package com.example.features.community

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AreaSafetyReport
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySafetyScreen(
  viewModel: CommunityViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  val filteredReports = remember(uiState.areaReports, uiState.searchQuery, uiState.selectedFilter) {
    uiState.areaReports.filter { report ->
      val matchesSearch = report.areaName.contains(uiState.searchQuery, ignoreCase = true) ||
          report.areaNameBn.contains(uiState.searchQuery, ignoreCase = true)

      val matchesFilter = when (uiState.selectedFilter) {
        "SAFE" -> report.safetyScore >= 80
        "CAUTION" -> report.isCautionZone || report.safetyScore < 70
        else -> true
      }
      matchesSearch && matchesFilter
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Community Zones & Live Share",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { viewModel.toggleReportingDialog(true) }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Report Safety")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
      item {
        Spacer(modifier = Modifier.height(4.dp))

        // Live Web Tracking Broadcast Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_live_share"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isLiveSharingEnabled) SafeGreenPrimary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant
          ),
          border = if (uiState.isLiveSharingEnabled) {
            androidx.compose.foundation.BorderStroke(1.5.dp, SafeGreenPrimary)
          } else null
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isLiveSharingEnabled) SafeGreenPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = if (uiState.isLiveSharingEnabled) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (uiState.isLiveSharingEnabled) "LIVE SHARING ACTIVE" else "LIVE GUARDIAN LINK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (uiState.isLiveSharingEnabled) SafeGreenDark else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (uiState.isLiveSharingEnabled) "Web link accessible by guardians" else "Share live GPS via browser link",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (uiState.isLiveSharingEnabled) {
                Button(
                  onClick = { viewModel.stopLiveSharing() },
                  colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("STOP", fontSize = 12.sp)
                }
              } else {
                Button(
                  onClick = { viewModel.startLiveSharing(60) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("START", fontSize = 12.sp)
                }
              }
            }

            if (uiState.isLiveSharingEnabled && uiState.activeLiveSession != null) {
              val session = uiState.activeLiveSession!!
              Spacer(modifier = Modifier.height(14.dp))
              HorizontalDivider(color = SafeGreenPrimary.copy(alpha = 0.3f))
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Live Tracking Code: ${session.trackingCode}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = session.shareableWebUrl,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                Row {
                  IconButton(
                    onClick = {
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      val clip = ClipData.newPlainText("RAMISA Tracking Link", session.shareableWebUrl)
                      clipboard.setPrimaryClip(clip)
                    }
                  ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Link", modifier = Modifier.size(18.dp))
                  }

                  IconButton(
                    onClick = {
                      val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Track my live safety location on RAMISA: ${session.shareableWebUrl}")
                        type = "text/plain"
                      }
                      context.startActivity(Intent.createChooser(sendIntent, "Share Tracking Link"))
                    }
                  ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }

      // Search and Filtering Header
      item {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = { viewModel.updateSearchQuery(it) },
          modifier = Modifier.fillMaxWidth(),
          placeholder = { Text("Search area, university, or street...") },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = uiState.selectedFilter == "ALL",
            onClick = { viewModel.setFilter("ALL") },
            label = { Text("All Dhaka Zones") }
          )
          FilterChip(
            selected = uiState.selectedFilter == "SAFE",
            onClick = { viewModel.setFilter("SAFE") },
            label = { Text("Verified Safe (80%+)") }
          )
          FilterChip(
            selected = uiState.selectedFilter == "CAUTION",
            onClick = { viewModel.setFilter("CAUTION") },
            label = { Text("Caution Zones") }
          )
        }
      }

      // Community Zone Reports List
      items(filteredReports, key = { it.id }) { report ->
        AreaReportCard(report = report)
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Crowdsource Safety Rating Dialog
  if (uiState.isReportingDialogVisible) {
    ReportSafetyDialog(
      onDismiss = { viewModel.toggleReportingDialog(false) },
      onSubmit = { name, nameBn, rating, lighting, isCaution, note ->
        viewModel.submitCommunityReport(name, nameBn, rating, lighting, isCaution, note)
      }
    )
  }
}

@Composable
fun AreaReportCard(
  report: AreaSafetyReport,
  modifier: Modifier = Modifier
) {
  val badgeColor = when {
    report.isCautionZone || report.safetyScore < 65 -> SafetyRedPrimary
    report.safetyScore >= 85 -> SafeGreenPrimary
    else -> Color(0xFFE65100)
  }

  Card(
    modifier = modifier.fillMaxWidth(),
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = report.areaName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = report.areaNameBn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = badgeColor.copy(alpha = 0.15f)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${report.safetyScore}/100",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
              color = badgeColor
            )
            Text(
              text = if (report.isCautionZone) "Caution" else "Safe",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
              color = badgeColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Attributes Grid (Lighting, Police, Crowd)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        AttributeChip(
          icon = Icons.Default.LightMode,
          label = "Lighting",
          value = "${report.lightingRating}★"
        )
        AttributeChip(
          icon = Icons.Default.LocalPolice,
          label = "Patrols",
          value = "${report.policePatrolRating}★"
        )
        AttributeChip(
          icon = Icons.Default.People,
          label = "Crowd",
          value = report.crowdDensity
        )
      }

      if (report.safetyTips.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))

        report.safetyTips.forEach { tip ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (report.isCautionZone) Icons.Default.Warning else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (report.isCautionZone) SafetyRedPrimary else SafeGreenPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = tip,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun AttributeChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label: $value",
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSafetyDialog(
  onDismiss: () -> Unit,
  onSubmit: (String, String, Int, Float, Boolean, String) -> Unit
) {
  var areaName by remember { mutableStateOf("") }
  var areaNameBn by remember { mutableStateOf("") }
  var scoreSlider by remember { mutableFloatStateOf(80f) }
  var lightingSlider by remember { mutableFloatStateOf(4.5f) }
  var isCaution by remember { mutableStateOf(false) }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Crowdsource Safety Report",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = areaName,
          onValueChange = { areaName = it },
          label = { Text("Area Name (English)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = areaNameBn,
          onValueChange = { areaNameBn = it },
          label = { Text("এলাকার নাম (বাংলা)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Safety Score: ${scoreSlider.toInt()}/100",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Slider(
          value = scoreSlider,
          onValueChange = { scoreSlider = it },
          valueRange = 20f..100f
        )

        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Helpful Tip or Warning") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(areaName, areaNameBn, scoreSlider.toInt(), lightingSlider, isCaution, note)
        }
      ) {
        Text("Submit Report")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
