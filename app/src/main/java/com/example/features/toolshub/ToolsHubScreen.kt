package com.example.features.toolshub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ToolItem(
  val title: String,
  val subtitle: String,
  val category: String,
  val icon: ImageVector,
  val onClick: () -> Unit,
  val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
  onNavigateToSafetyGuide: () -> Unit,
  onNavigateToCyberCrimeSupport: () -> Unit,
  onNavigateToLegalAid: () -> Unit,
  onNavigateToFirstAid: () -> Unit,
  onNavigateToWellness: () -> Unit,
  onNavigateToSelfDefense: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit,
  onNavigateToSafePlaces: () -> Unit,
  onNavigateToThreatGuard: () -> Unit,
  onNavigateToRideSafety: () -> Unit,
  onNavigateToChildSafety: () -> Unit,
  onNavigateToCampusSafety: () -> Unit,
  onNavigateToMeshNetwork: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToStealthMode: () -> Unit,
  onNavigateToVault: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToBatteryPing: () -> Unit,
  onNavigateToOfflineMaps: () -> Unit,
  onNavigateToVolunteerEscort: () -> Unit,
  onNavigateToGuardianCircle: () -> Unit,
  onNavigateToCommunitySafety: () -> Unit,
  onNavigateToFakeCall: () -> Unit,
  onNavigateToSirenStrobe: () -> Unit,
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToContacts: () -> Unit,
  onNavigateToAdmin: () -> Unit = {},
  onTriggerSos: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf("All") }
  val categories = listOf("All", "Legal & Rights", "Medical & Wellness", "Protection & Transit", "Security & Vault")

  val tools = listOf(
    ToolItem(
      title = "Legal Guide & Rights",
      subtitle = "Penal codes & emergency protocols",
      category = "Legal & Rights",
      icon = Icons.AutoMirrored.Filled.Assignment,
      onClick = onNavigateToSafetyGuide,
      testTag = "tool_legal_guide"
    ),
    ToolItem(
      title = "Cyber Support Desk",
      subtitle = "PCSW cyber crime reporting",
      category = "Legal & Rights",
      icon = Icons.Default.SupportAgent,
      onClick = onNavigateToCyberCrimeSupport,
      testTag = "tool_cyber_desk"
    ),
    ToolItem(
      title = "Free Legal Aid & GD",
      subtitle = "16430 helpline & GD drafter",
      category = "Legal & Rights",
      icon = Icons.Default.Gavel,
      onClick = onNavigateToLegalAid,
      testTag = "tool_legal_aid"
    ),
    ToolItem(
      title = "First Aid & Triage",
      subtitle = "Offline medical emergency guide",
      category = "Medical & Wellness",
      icon = Icons.Default.LocalHospital,
      onClick = onNavigateToFirstAid,
      testTag = "tool_first_aid"
    ),
    ToolItem(
      title = "Wellness & Counseling",
      subtitle = "Trauma recovery & support",
      category = "Medical & Wellness",
      icon = Icons.Default.Spa,
      onClick = onNavigateToWellness,
      testTag = "tool_wellness"
    ),
    ToolItem(
      title = "Safety Timer",
      subtitle = "Dead Man's automatic check-in",
      category = "Medical & Wellness",
      icon = Icons.Default.Timer,
      onClick = onNavigateToSafetyTimer,
      testTag = "tool_safety_timer"
    ),
    ToolItem(
      title = "Safe Places Radar",
      subtitle = "Thanas & 24/7 safe hubs",
      category = "Protection & Transit",
      icon = Icons.Default.Place,
      onClick = onNavigateToSafePlaces,
      testTag = "tool_safe_places"
    ),
    ToolItem(
      title = "Offline Maps",
      subtitle = "Zero-internet map access",
      category = "Protection & Transit",
      icon = Icons.Default.Map,
      onClick = onNavigateToOfflineMaps,
      testTag = "tool_offline_maps"
    ),
    ToolItem(
      title = "AI Threat Guard",
      subtitle = "Acoustic scream detection",
      category = "Protection & Transit",
      icon = Icons.Default.Sensors,
      onClick = onNavigateToThreatGuard,
      testTag = "tool_threat_guard"
    ),
    ToolItem(
      title = "Rickshaw & Ride Guard",
      subtitle = "Vehicle plate & route tracking",
      category = "Protection & Transit",
      icon = Icons.AutoMirrored.Filled.DirectionsWalk,
      onClick = onNavigateToRideSafety,
      testTag = "tool_ride_safety"
    ),
    ToolItem(
      title = "Child Safety",
      subtitle = "Geofencing & 1098 helpline",
      category = "Protection & Transit",
      icon = Icons.Default.ChildCare,
      onClick = onNavigateToChildSafety,
      testTag = "tool_child_safety"
    ),
    ToolItem(
      title = "Campus Safety",
      subtitle = "Safe corridors & escort service",
      category = "Protection & Transit",
      icon = Icons.Default.School,
      onClick = onNavigateToCampusSafety,
      testTag = "tool_campus_safety"
    ),
    ToolItem(
      title = "Peer Escort Network",
      subtitle = "Volunteer walking guardian",
      category = "Protection & Transit",
      icon = Icons.Default.People,
      onClick = onNavigateToVolunteerEscort,
      testTag = "tool_peer_escort"
    ),
    ToolItem(
      title = "Offline Mesh Network",
      subtitle = "Zero-internet BLE SOS relay",
      category = "Security & Vault",
      icon = Icons.Default.Hub,
      onClick = onNavigateToMeshNetwork,
      testTag = "tool_mesh_network"
    ),
    ToolItem(
      title = "Hardware 3x SOS",
      subtitle = "Power button & band triggers",
      category = "Security & Vault",
      icon = Icons.Default.FlashOn,
      onClick = onNavigateToHardwareTrigger,
      testTag = "tool_hardware_trigger"
    ),
    ToolItem(
      title = "Stealth Calculator",
      subtitle = "Camouflage PIN mode",
      category = "Security & Vault",
      icon = Icons.Default.Calculate,
      onClick = onNavigateToStealthMode,
      testTag = "tool_stealth_calc"
    ),
    ToolItem(
      title = "Safety Vault",
      subtitle = "Encrypted audio evidence log",
      category = "Security & Vault",
      icon = Icons.Default.Lock,
      onClick = onNavigateToVault,
      testTag = "tool_vault"
    ),
    ToolItem(
      title = "Incident History",
      subtitle = "Timestamped alert records",
      category = "Security & Vault",
      icon = Icons.Default.History,
      onClick = onNavigateToHistory,
      testTag = "tool_history"
    ),
    ToolItem(
      title = "Low-Battery Auto-Ping",
      subtitle = "Offline location broadcast on low battery",
      category = "Security & Vault",
      icon = androidx.compose.material.icons.Icons.Default.BatteryAlert,
      onClick = onNavigateToBatteryPing,
      testTag = "tool_battery_ping"
    ),
    ToolItem(
      title = "Safety Control Room (Admin)",
      subtitle = "User roster, NID/ID verify & dispatch console",
      category = "Security & Vault",
      icon = Icons.Default.AdminPanelSettings,
      onClick = onNavigateToAdmin,
      testTag = "tool_admin_panel"
    )
  )

  val filteredTools = if (selectedCategory == "All") tools else tools.filter { it.category == selectedCategory }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Tools Hub",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimaryDark
            )
            Text(
              text = "Category-wise safety resources & support",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Spacer(modifier = Modifier.height(4.dp))

      // Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { category ->
          val isSelected = selectedCategory == category
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = category },
            label = { Text(category, style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PinkPrimary,
              selectedLabelColor = Color.White,
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = TextPrimaryLight
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = PinkBorderSubtle,
              selectedBorderColor = PinkPrimary
            )
          )
        }
      }

      // Tools List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredTools) { tool ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = tool.onClick)
              .testTag(tool.testTag),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, PinkBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = PinkAccentLilac.copy(alpha = 0.8f),
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = PinkPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(16.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = tool.title,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  ),
                  color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = tool.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondaryLight
                )
              }

              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open tool",
                tint = TextSecondaryLight,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }
}
