package com.example.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkAccentLilac
import com.example.ui.theme.PinkBorderSubtle
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkHeaderGradientEnd
import com.example.ui.theme.PinkHeaderGradientStart
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark
import com.example.ui.theme.PinkPrimaryLight
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

enum class SafetyCategoryTab(val label: String, val icon: ImageVector) {
  OVERVIEW("Overview", Icons.Default.Shield),
  EMERGENCY("Emergency", Icons.Default.Lock),
  NAVIGATION("Navigation", Icons.Default.DirectionsWalk),
  SENSORS("Shield & AI", Icons.Default.Sensors),
  COMMUNITY("Care & Legal", Icons.Default.Group)
}

@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToContacts: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onTriggerSos: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  onNavigateToSafePlaces: () -> Unit = {},
  onNavigateToFakeCall: () -> Unit = {},
  onNavigateToSafetyGuide: () -> Unit = {},
  onNavigateToThreatGuard: () -> Unit = {},
  onNavigateToCommunitySafety: () -> Unit = {},
  onNavigateToHardwareTrigger: () -> Unit = {},
  onNavigateToMeshNetwork: () -> Unit = {},
  onNavigateToVault: () -> Unit = {},
  onNavigateToGuardianCircle: () -> Unit = {},
  onNavigateToStealthMode: () -> Unit = {},
  onNavigateToSirenStrobe: () -> Unit = {},
  onNavigateToRideSafety: () -> Unit = {},
  onNavigateToCampusSafety: () -> Unit = {},
  onNavigateToSelfDefense: () -> Unit = {},
  onNavigateToCyberCrimeSupport: () -> Unit = {},
  onNavigateToWellness: () -> Unit = {},
  onNavigateToVolunteerEscort: () -> Unit = {},
  onNavigateToLegalAid: () -> Unit = {},
  onNavigateToSafetyTimer: () -> Unit = {},
  onNavigateToFirstAid: () -> Unit = {},
  onNavigateToChildSafety: () -> Unit = {},
  onNavigateToLockScreenSos: () -> Unit = {},
  onNavigateToDangerMap: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()
  var selectedTab by remember { mutableStateOf(SafetyCategoryTab.OVERVIEW) }

  // Pulsing animation for the SOS halo
  val infiniteTransition = rememberInfiniteTransition(label = "sos_halo")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. TOP HEADER: Clean Branded Banner & Hotlines
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                colors = listOf(PinkHeaderGradientStart, PinkHeaderGradientEnd)
              ),
              shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "RAMISA",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 2.sp
                ),
                color = Color.White,
                modifier = Modifier.testTag("app_logo_title")
              )
              Text(
                text = "Women & Child Personal Safety",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.92f)
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Essential App Status (Active Background Service notification)
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.testTag("badge_background_service")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF4CAF50))
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Service Active",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = Color.White
                  )
                }
              }

              // User Profile Avatar
              IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color.White)
                  .testTag("btn_home_profile")
              ) {
                Text(
                  text = "SR",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark
                )
              }
            }
          }
        }
      }

      // 2. STATUS BANNER (Compact & Clean)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("safety_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PinkContainer),
        border = BorderStroke(1.dp, PinkBorderSubtle)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PinkPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CheckCircle,
              contentDescription = "Status Safe",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "SHIELD ACTIVE",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                ),
                color = PinkPrimaryDark,
                modifier = Modifier.testTag("status_text")
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .clip(CircleShape)
                  .background(SafeGreenDark)
              )
            }
            Text(
              text = "Live Protection • ${uiState.activeContactCount} Trusted Guardians Ready",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 3. CENTRAL SOS BUTTON (Large, Distinct Pink Floating Action Button - SOS - PRESS FOR HELP)
      Box(
        modifier = Modifier
          .size(200.dp)
          .testTag("sos_button_container"),
        contentAlignment = Alignment.Center
      ) {
        // Outer radiant pulse
        Box(
          modifier = Modifier
            .size(195.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  PinkPrimaryLight.copy(alpha = 0.45f),
                  PinkPrimary.copy(alpha = 0.2f),
                  Color.Transparent
                )
              )
            )
        )

        // Distinct Pink SOS Floating Action Button
        Surface(
          modifier = Modifier
            .size(165.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onTriggerSos
            )
            .testTag("sos_button"),
          shape = CircleShape,
          color = Color.Transparent
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(PinkPrimaryLight, PinkPrimaryDark)
                )
              )
              .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(horizontal = 8.dp)
            ) {
              Text(
                text = "SOS",
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 3.sp
                ),
                color = Color.White
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "PRESS FOR HELP",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 1.2.sp
                ),
                color = Color.White
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. IMMEDIATE EMERGENCY SHORTCUTS (4 Core Priority Tools)
      Text(
        text = "PRIORITY EMERGENCY CONTROLS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickEmergencyPill(
          title = "LockScreen SOS",
          icon = Icons.Default.Lock,
          accentColor = SafetyRedPrimary,
          onClick = onNavigateToLockScreenSos,
          modifier = Modifier.weight(1f),
          testTag = "btn_lockscreen_sos_guard"
        )

        QuickEmergencyPill(
          title = "Fake Call",
          icon = Icons.Default.PhoneCallback,
          accentColor = PinkPrimary,
          onClick = onNavigateToFakeCall,
          modifier = Modifier.weight(1f),
          testTag = "btn_fake_call"
        )

        QuickEmergencyPill(
          title = "Siren / Strobe",
          icon = Icons.Default.VolumeUp,
          accentColor = PinkPrimary,
          onClick = onNavigateToSirenStrobe,
          modifier = Modifier.weight(1f),
          testTag = "btn_siren_strobe"
        )

        QuickEmergencyPill(
          title = "Safe Journey",
          icon = Icons.Default.DirectionsWalk,
          accentColor = PinkPrimary,
          onClick = onNavigateToSafeJourney,
          modifier = Modifier.weight(1f),
          testTag = "btn_safe_journey"
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 5. STRUCTURED CATEGORY TABS (Removes Visual Clutter & Long Random Scrolls)
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(SafetyCategoryTab.values()) { tab ->
          val isSelected = selectedTab == tab
          FilterChip(
            selected = isSelected,
            onClick = { selectedTab = tab },
            label = {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              )
            },
            leadingIcon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) PinkPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PinkContainer,
              selectedLabelColor = PinkPrimaryDark,
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = PinkBorderSubtle,
              selectedBorderColor = PinkPrimary,
              borderWidth = 1.dp
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 6. CATEGORIZED CONTROL PANELS
      when (selectedTab) {
        SafetyCategoryTab.OVERVIEW -> {
          OverviewSection(
            onNavigateToContacts = onNavigateToContacts,
            onNavigateToSafePlaces = onNavigateToSafePlaces,
            onNavigateToDangerMap = onNavigateToDangerMap,
            onNavigateToThreatGuard = onNavigateToThreatGuard,
            onNavigateToGuardianCircle = onNavigateToGuardianCircle,
            onNavigateToHardwareTrigger = onNavigateToHardwareTrigger,
            onNavigateToMeshNetwork = onNavigateToMeshNetwork,
            onNavigateToRideSafety = onNavigateToRideSafety,
            onNavigateToCampusSafety = onNavigateToCampusSafety,
            onNavigateToChildSafety = onNavigateToChildSafety,
            onNavigateToVault = onNavigateToVault,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToCommunitySafety = onNavigateToCommunitySafety,
            onNavigateToSelfDefense = onNavigateToSelfDefense,
            onNavigateToStealthMode = onNavigateToStealthMode,
            onNavigateToSafetyGuide = onNavigateToSafetyGuide,
            onNavigateToCyberCrimeSupport = onNavigateToCyberCrimeSupport,
            onNavigateToVolunteerEscort = onNavigateToVolunteerEscort,
            onNavigateToLegalAid = onNavigateToLegalAid,
            onNavigateToSafetyTimer = onNavigateToSafetyTimer,
            onNavigateToFirstAid = onNavigateToFirstAid,
            onNavigateToWellness = onNavigateToWellness,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToFakeCall = onNavigateToFakeCall,
            onNavigateToSirenStrobe = onNavigateToSirenStrobe,
            onNavigateToSafeJourney = onNavigateToSafeJourney
          )
        }

        SafetyCategoryTab.EMERGENCY -> {
          EmergencyTabSection(
            onNavigateToLockScreenSos = onNavigateToLockScreenSos,
            onNavigateToFakeCall = onNavigateToFakeCall,
            onNavigateToSirenStrobe = onNavigateToSirenStrobe,
            onNavigateToContacts = onNavigateToContacts,
            onNavigateToMeshNetwork = onNavigateToMeshNetwork,
            onNavigateToHardwareTrigger = onNavigateToHardwareTrigger,
            onNavigateToSafetyTimer = onNavigateToSafetyTimer
          )
        }

        SafetyCategoryTab.NAVIGATION -> {
          NavigationTabSection(
            onNavigateToSafeJourney = onNavigateToSafeJourney,
            onNavigateToDangerMap = onNavigateToDangerMap,
            onNavigateToSafePlaces = onNavigateToSafePlaces,
            onNavigateToRideSafety = onNavigateToRideSafety,
            onNavigateToCommunitySafety = onNavigateToCommunitySafety
          )
        }

        SafetyCategoryTab.SENSORS -> {
          SensorsTabSection(
            onNavigateToThreatGuard = onNavigateToThreatGuard,
            onNavigateToHardwareTrigger = onNavigateToHardwareTrigger,
            onNavigateToStealthMode = onNavigateToStealthMode,
            onNavigateToVault = onNavigateToVault,
            onNavigateToSafetyTimer = onNavigateToSafetyTimer
          )
        }

        SafetyCategoryTab.COMMUNITY -> {
          CommunityTabSection(
            onNavigateToGuardianCircle = onNavigateToGuardianCircle,
            onNavigateToCampusSafety = onNavigateToCampusSafety,
            onNavigateToChildSafety = onNavigateToChildSafety,
            onNavigateToVolunteerEscort = onNavigateToVolunteerEscort,
            onNavigateToLegalAid = onNavigateToLegalAid,
            onNavigateToSafetyGuide = onNavigateToSafetyGuide,
            onNavigateToCyberCrimeSupport = onNavigateToCyberCrimeSupport,
            onNavigateToFirstAid = onNavigateToFirstAid,
            onNavigateToWellness = onNavigateToWellness,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSelfDefense = onNavigateToSelfDefense,
            onNavigateToSettings = onNavigateToSettings
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 7. BANGLADESH NATIONAL HELPLINE DIRECT BAR
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hotline_info_bar"),
        shape = RoundedCornerShape(14.dp),
        color = PinkContainer,
        border = BorderStroke(1.dp, PinkBorderSubtle)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = PinkPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Women Helpline: 109 • Child Helpline: 1098",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = PinkPrimaryDark
            )
          }

          Text(
            text = "Toll Free",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = SafeGreenDark
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

// -------------------------------------------------------------
// TAB CONTENT IMPLEMENTATIONS (Clean, Standardized 2-Column Grid)
// -------------------------------------------------------------

@Composable
private fun OverviewSection(
  onNavigateToContacts: () -> Unit,
  onNavigateToSafePlaces: () -> Unit,
  onNavigateToDangerMap: () -> Unit,
  onNavigateToThreatGuard: () -> Unit,
  onNavigateToGuardianCircle: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToMeshNetwork: () -> Unit,
  onNavigateToRideSafety: () -> Unit,
  onNavigateToCampusSafety: () -> Unit,
  onNavigateToChildSafety: () -> Unit,
  onNavigateToVault: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToCommunitySafety: () -> Unit,
  onNavigateToSelfDefense: () -> Unit,
  onNavigateToStealthMode: () -> Unit,
  onNavigateToSafetyGuide: () -> Unit,
  onNavigateToCyberCrimeSupport: () -> Unit,
  onNavigateToVolunteerEscort: () -> Unit,
  onNavigateToLegalAid: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit,
  onNavigateToFirstAid: () -> Unit,
  onNavigateToWellness: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToFakeCall: () -> Unit,
  onNavigateToSirenStrobe: () -> Unit,
  onNavigateToSafeJourney: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // Interactive Map Radar Preview
    com.example.features.map.DangerMapHomeWidget(
      onOpenFullMap = onNavigateToDangerMap
    )

    // Standardized 2-Column Safety Controls Grid (Max 6 Core Emergency Cards)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      SafetyMiniCard(
        title = "Fake Escape Call",
        subtitle = "Discreet escape call simulator",
        icon = Icons.Default.PhoneCallback,
        onClick = onNavigateToFakeCall,
        modifier = Modifier.weight(1f),
        testTag = "btn_fake_call"
      )

      SafetyMiniCard(
        title = "Safe Journey",
        subtitle = "Live GPS route tracking",
        icon = Icons.Default.DirectionsWalk,
        onClick = onNavigateToSafeJourney,
        modifier = Modifier.weight(1f),
        testTag = "btn_safe_journey"
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      SafetyMiniCard(
        title = "Safe Places Radar",
        subtitle = "Thanas & 24/7 hospitals",
        icon = Icons.Default.Place,
        onClick = onNavigateToSafePlaces,
        modifier = Modifier.weight(1f),
        testTag = "btn_safe_places"
      )

      SafetyMiniCard(
        title = "Emergency Siren",
        subtitle = "110dB alarm & strobe",
        icon = Icons.Default.VolumeUp,
        onClick = onNavigateToSirenStrobe,
        modifier = Modifier.weight(1f),
        testTag = "btn_siren_strobe"
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      SafetyMiniCard(
        title = "Offline Mesh",
        subtitle = "Zero-net distress beacon",
        icon = Icons.Default.Hub,
        onClick = onNavigateToMeshNetwork,
        modifier = Modifier.weight(1f),
        testTag = "btn_mesh_network"
      )

      SafetyMiniCard(
        title = "Hardware 3x SOS",
        subtitle = "Power button triggers",
        icon = Icons.Default.FlashOn,
        onClick = onNavigateToHardwareTrigger,
        modifier = Modifier.weight(1f),
        testTag = "btn_hardware_triggers"
      )
    }
  }
}

@Composable
private fun EmergencyTabSection(
  onNavigateToLockScreenSos: () -> Unit,
  onNavigateToFakeCall: () -> Unit,
  onNavigateToSirenStrobe: () -> Unit,
  onNavigateToContacts: () -> Unit,
  onNavigateToMeshNetwork: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SafetyActionCard(
      title = "LOCK SCREEN & OFFLINE SOS",
      subtitle = "1-Tap SOS over locked screen • Direct GSM SMS & Mesh without opening app",
      icon = Icons.Default.Lock,
      accentColor = SafetyRedPrimary,
      onClick = onNavigateToLockScreenSos,
      testTag = "btn_lockscreen_sos_guard"
    )

    SafetyActionCard(
      title = "EMERGENCY CONTACTS",
      subtitle = "Manage up to 7 trusted contacts with instant SOS SMS broadcast",
      icon = Icons.Default.Contacts,
      accentColor = PinkPrimary,
      onClick = onNavigateToContacts,
      testTag = "btn_emergency_contacts"
    )

    SafetyActionCard(
      title = "DISCREET FAKE ESCAPE CALL",
      subtitle = "Simulate realistic incoming call with speaking guidance",
      icon = Icons.Default.PhoneCallback,
      accentColor = PinkPrimary,
      onClick = onNavigateToFakeCall,
      testTag = "btn_fake_call"
    )

    SafetyActionCard(
      title = "EMERGENCY SIREN & 8HZ STROBE",
      subtitle = "110dB high acoustic whistle & disorienting defense strobe",
      icon = Icons.Default.VolumeUp,
      accentColor = PinkPrimary,
      onClick = onNavigateToSirenStrobe,
      testTag = "btn_siren_strobe"
    )

    SafetyActionCard(
      title = "OFFLINE MESH NETWORK",
      subtitle = "Peer-to-peer Wi-Fi Aware distress broadcast without mobile data",
      icon = Icons.Default.Hub,
      accentColor = PinkPrimary,
      onClick = onNavigateToMeshNetwork,
      testTag = "btn_mesh_network"
    )

    SafetyActionCard(
      title = "HARDWARE & POWER BUTTON SOS",
      subtitle = "Triple press power key or volume key trigger",
      icon = Icons.Default.FlashOn,
      accentColor = PinkPrimary,
      onClick = onNavigateToHardwareTrigger,
      testTag = "btn_hardware_triggers"
    )

    SafetyActionCard(
      title = "SAFETY CHECK-IN TIMER",
      subtitle = "Dead Man's countdown switch with auto alert on expiry",
      icon = Icons.Default.Timer,
      accentColor = PinkPrimary,
      onClick = onNavigateToSafetyTimer,
      testTag = "btn_safety_timer"
    )
  }
}

@Composable
private fun NavigationTabSection(
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToDangerMap: () -> Unit,
  onNavigateToSafePlaces: () -> Unit,
  onNavigateToRideSafety: () -> Unit,
  onNavigateToCommunitySafety: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    com.example.features.map.DangerMapHomeWidget(
      onOpenFullMap = onNavigateToDangerMap
    )

    SafetyActionCard(
      title = "SAFE JOURNEY TRACKER",
      subtitle = "Live path monitoring, estimated arrival & safety checks",
      icon = Icons.Default.DirectionsWalk,
      accentColor = PinkPrimary,
      onClick = onNavigateToSafeJourney,
      testTag = "btn_safe_journey"
    )

    SafetyActionCard(
      title = "SAFE PLACES RADAR",
      subtitle = "Verified Thanas, 24/7 Hospitals & Women Support booths",
      icon = Icons.Default.Place,
      accentColor = PinkPrimary,
      onClick = onNavigateToSafePlaces,
      testTag = "btn_safe_places"
    )

    SafetyActionCard(
      title = "RICKSHAW & TRANSIT GUARD",
      subtitle = "Vehicle plate logger, ride timer & route deviation detection",
      icon = Icons.Default.DirectionsBus,
      accentColor = PinkPrimary,
      onClick = onNavigateToRideSafety,
      testTag = "btn_ride_safety"
    )

    SafetyActionCard(
      title = "COMMUNITY SAFETY ZONES",
      subtitle = "Crowdsourced incident ratings and live guardian sharing",
      icon = Icons.Default.Share,
      accentColor = PinkPrimary,
      onClick = onNavigateToCommunitySafety,
      testTag = "btn_community_safety"
    )
  }
}

@Composable
private fun SensorsTabSection(
  onNavigateToThreatGuard: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToStealthMode: () -> Unit,
  onNavigateToVault: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SafetyActionCard(
      title = "AI ACOUSTIC THREAT GUARD",
      subtitle = "Automated scream, physical impact & high-frequency shock detection",
      icon = Icons.Default.Sensors,
      accentColor = PinkPrimary,
      onClick = onNavigateToThreatGuard,
      testTag = "btn_threat_guard"
    )

    SafetyActionCard(
      title = "HARDWARE & WEARABLE TRIGGERS",
      subtitle = "Power button 3x, discreet volume hold, & BLE Smart Band SOS",
      icon = Icons.Default.FlashOn,
      accentColor = PinkPrimary,
      onClick = onNavigateToHardwareTrigger,
      testTag = "btn_hardware_triggers"
    )

    SafetyActionCard(
      title = "CAMOUFLAGE CALCULATOR",
      subtitle = "Stealth calculator PIN interface for hidden SOS activation",
      icon = Icons.Default.Calculate,
      accentColor = PinkPrimary,
      onClick = onNavigateToStealthMode,
      testTag = "btn_stealth_calc"
    )

    SafetyActionCard(
      title = "ENCRYPTED EVIDENCE VAULT",
      subtitle = "Tamper-proof AES-256 audio and incident recording vault",
      icon = Icons.Default.Lock,
      accentColor = PinkPrimary,
      onClick = onNavigateToVault,
      testTag = "btn_vault"
    )

    SafetyActionCard(
      title = "DEAD MAN'S SAFETY TIMER",
      subtitle = "Proactive timeout countdown before venturing into isolated areas",
      icon = Icons.Default.Timer,
      accentColor = PinkPrimary,
      onClick = onNavigateToSafetyTimer,
      testTag = "btn_safety_timer"
    )
  }
}

@Composable
private fun CommunityTabSection(
  onNavigateToGuardianCircle: () -> Unit,
  onNavigateToCampusSafety: () -> Unit,
  onNavigateToChildSafety: () -> Unit,
  onNavigateToVolunteerEscort: () -> Unit,
  onNavigateToLegalAid: () -> Unit,
  onNavigateToSafetyGuide: () -> Unit,
  onNavigateToCyberCrimeSupport: () -> Unit,
  onNavigateToFirstAid: () -> Unit,
  onNavigateToWellness: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToSelfDefense: () -> Unit,
  onNavigateToSettings: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SafetyActionCard(
      title = "GUARDIAN CIRCLES",
      subtitle = "Real-time peer network, battery health & mutual check-ins",
      icon = Icons.Default.Group,
      accentColor = PinkPrimary,
      onClick = onNavigateToGuardianCircle,
      testTag = "btn_guardian_circles"
    )

    SafetyActionCard(
      title = "CAMPUS SAFETY & ESCORT",
      subtitle = "University safe corridors, proctor desk & student escort",
      icon = Icons.Default.School,
      accentColor = PinkPrimary,
      onClick = onNavigateToCampusSafety,
      testTag = "btn_campus_safety"
    )

    SafetyActionCard(
      title = "CHILD & TEEN SAFETY",
      subtitle = "Live geofence zones, battery telemetry & 1098 helpline",
      icon = Icons.Default.ChildCare,
      accentColor = PinkPrimary,
      onClick = onNavigateToChildSafety,
      testTag = "btn_child_safety"
    )

    SafetyActionCard(
      title = "VERIFIED VOLUNTEER ESCORTS",
      subtitle = "Community walking partners for late-night safety",
      icon = Icons.Default.People,
      accentColor = PinkPrimary,
      onClick = onNavigateToVolunteerEscort,
      testTag = "btn_volunteer_escort"
    )

    SafetyActionCard(
      title = "LEGAL AID & POLICE GD DRAFTER",
      subtitle = "Bangladesh legal rights, automated GD generator & 16430 aid",
      icon = Icons.Default.Gavel,
      accentColor = PinkPrimary,
      onClick = onNavigateToLegalAid,
      testTag = "btn_legal_aid"
    )

    SafetyActionCard(
      title = "POLICE CYBER SUPPORT (PCSW)",
      subtitle = "Direct unit for online harassment, blackmail & digital safety",
      icon = Icons.Default.SupportAgent,
      accentColor = PinkPrimary,
      onClick = onNavigateToCyberCrimeSupport,
      testTag = "btn_cyber_desk"
    )

    SafetyActionCard(
      title = "SELF-DEFENSE TECHNIQUES",
      subtitle = "Visual step-by-step physical defense and escape maneuvers",
      icon = Icons.Default.SportsMartialArts,
      accentColor = PinkPrimary,
      onClick = onNavigateToSelfDefense,
      testTag = "btn_self_defense"
    )

    SafetyActionCard(
      title = "OFFLINE FIRST AID GUIDE",
      subtitle = "Immediate medical emergency instructions and triage",
      icon = Icons.Default.LocalHospital,
      accentColor = PinkPrimary,
      onClick = onNavigateToFirstAid,
      testTag = "btn_first_aid"
    )

    SafetyActionCard(
      title = "WELLNESS & TRAUMA COUNSELING",
      subtitle = "24/7 mental health support, Kaan Pete Roi (01779554391)",
      icon = Icons.Default.Spa,
      accentColor = PinkPrimary,
      onClick = onNavigateToWellness,
      testTag = "btn_wellness_care"
    )

    SafetyActionCard(
      title = "LEGAL SAFETY HANDBOOK",
      subtitle = "Women's rights under Bangladesh Penal Code & prevention acts",
      icon = Icons.Default.AutoStories,
      accentColor = PinkPrimary,
      onClick = onNavigateToSafetyGuide,
      testTag = "btn_safety_guide"
    )

    SafetyActionCard(
      title = "INCIDENT HISTORY LOGS",
      subtitle = "Timestamped history of SOS alerts and test beacons",
      icon = Icons.Default.History,
      accentColor = PinkPrimary,
      onClick = onNavigateToHistory,
      testTag = "btn_history"
    )

    SafetyActionCard(
      title = "APPLICATION SETTINGS",
      subtitle = "Manage security PIN, sensitivity thresholds & preferences",
      icon = Icons.Default.Settings,
      accentColor = PinkPrimary,
      onClick = onNavigateToSettings,
      testTag = "btn_settings"
    )
  }
}

// -------------------------------------------------------------
// REUSABLE COMPONENTS
// -------------------------------------------------------------

@Composable
private fun QuickEmergencyPill(
  title: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  Card(
    modifier = modifier
      .testTag(testTag)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, PinkBorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(PinkAccentLilac.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = PinkPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        ),
        color = TextPrimaryLight,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun SafetyActionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, PinkBorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PinkAccentLilac.copy(alpha = 0.7f),
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
          ),
          color = TextPrimaryLight
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = TextSecondaryLight
        )
      }
    }
  }
}

@Composable
private fun SafetyMiniCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  Card(
    modifier = modifier
      .height(100.dp)
      .testTag(testTag)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, PinkBorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Surface(
        shape = CircleShape,
        color = PinkAccentLilac.copy(alpha = 0.7f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.size(17.dp)
          )
        }
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          ),
          color = TextPrimaryLight,
          maxLines = 1
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = TextSecondaryLight,
          maxLines = 1
        )
      }
    }
  }
}
