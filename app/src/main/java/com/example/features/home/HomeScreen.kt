package com.example.features.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary

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
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

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
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Header: App Title & Profile / Hotline
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "RAMISA",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("app_logo_title")
          )
          Text(
            text = "Women & Child Personal Safety",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // National Emergency Hotline Quick Pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = SafetyRedLight.copy(alpha = 0.15f),
            modifier = Modifier.testTag("badge_hotline_999")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "999 Hotline",
                tint = SafetyRedDark,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "999",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SafetyRedDark
              )
            }
          }

          // User Profile Avatar Button
          IconButton(
            onClick = onNavigateToProfile,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .testTag("btn_home_profile")
          ) {
            Text(
              text = "SR",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Safety Status Banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("safety_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = SafeGreenContainer
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SafeGreenPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CheckCircle,
              contentDescription = "Status Safe",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "STATUS: SAFE",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = SafeGreenDark,
                modifier = Modifier.testTag("status_text")
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(SafeGreenPrimary)
              )
            }
            Text(
              text = "All protection systems active • ${uiState.activeContactCount} trusted contacts ready",
              style = MaterialTheme.typography.bodySmall,
              color = SafeGreenDark.copy(alpha = 0.85f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Central Prominent SOS Button
      Box(
        modifier = Modifier
          .size(220.dp)
          .testTag("sos_button_container"),
        contentAlignment = Alignment.Center
      ) {
        // Outer glowing halo
        Box(
          modifier = Modifier
            .size(210.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  SafetyRedLight.copy(alpha = 0.35f),
                  SafetyRedPrimary.copy(alpha = 0.15f),
                  Color.Transparent
                )
              )
            )
        )

        // Main Circular Button
        Surface(
          modifier = Modifier
            .size(175.dp)
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
                  colors = listOf(SafetyRedLight, SafetyRedDark)
                )
              )
              .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Text(
                text = "🔴 SOS",
                style = MaterialTheme.typography.displaySmall.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 2.sp
                ),
                color = Color.White
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "PRESS FOR HELP",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Instantly alerts your trusted contacts & prepares location fallback",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Main Navigation Action Grid
      Text(
        text = "SAFETY CONTROLS",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      )

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // SAFE JOURNEY BUTTON
        SafetyActionCard(
          title = "SAFE JOURNEY",
          subtitle = "Track route, arrival estimate & proactive safety check",
          icon = Icons.Default.DirectionsWalk,
          accentColor = MaterialTheme.colorScheme.primary,
          onClick = onNavigateToSafeJourney,
          testTag = "btn_safe_journey"
        )

        // EMERGENCY CONTACTS BUTTON
        SafetyActionCard(
          title = "EMERGENCY CONTACTS",
          subtitle = "Manage up to 7 trusted contacts for instant SOS alert",
          icon = Icons.Default.Contacts,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToContacts,
          testTag = "btn_emergency_contacts"
        )

        // Row for SAFE PLACES RADAR & FAKE ESCAPE CALL
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // SAFE PLACES RADAR
          SafetyMiniCard(
            title = "SAFE PLACES",
            subtitle = "Thanas & Hospitals",
            icon = Icons.Default.Place,
            onClick = onNavigateToSafePlaces,
            modifier = Modifier.weight(1f),
            testTag = "btn_safe_places"
          )

          // FAKE ESCAPE CALL
          SafetyMiniCard(
            title = "FAKE CALL",
            subtitle = "Discreet escape call",
            icon = Icons.Default.PhoneCallback,
            onClick = onNavigateToFakeCall,
            modifier = Modifier.weight(1f),
            testTag = "btn_fake_call"
          )
        }

        // AI THREAT GUARD & ACOUSTIC MONITOR
        SafetyActionCard(
          title = "AI THREAT GUARD",
          subtitle = "Acoustic scream & shock sensor with auto-SOS trigger",
          icon = Icons.Default.Sensors,
          accentColor = Color(0xFFE65100),
          onClick = onNavigateToThreatGuard,
          testTag = "btn_threat_guard"
        )

        // COMMUNITY SAFETY ZONES & LIVE SHARING
        SafetyActionCard(
          title = "COMMUNITY ZONES & LIVE SHARE",
          subtitle = "Dhaka safety heatmap ratings & shareable guardian link",
          icon = Icons.Default.Share,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToCommunitySafety,
          testTag = "btn_community_safety"
        )

        // HARDWARE & WEARABLE TRIGGERS
        SafetyActionCard(
          title = "HARDWARE & WEARABLE TRIGGERS",
          subtitle = "Power 3x press, discreet volume hold, & BLE Smart Band SOS",
          icon = Icons.Default.FlashOn,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToHardwareTrigger,
          testTag = "btn_hardware_triggers"
        )

        // ZERO-INTERNET OFFLINE MESH SOS
        SafetyActionCard(
          title = "ZERO-INTERNET OFFLINE MESH",
          subtitle = "Multi-hop peer-to-peer Wi-Fi Aware & Bluetooth distress beacon",
          icon = Icons.Default.Hub,
          accentColor = MaterialTheme.colorScheme.primary,
          onClick = onNavigateToMeshNetwork,
          testTag = "btn_mesh_network"
        )

        // GUARDIAN CIRCLES LIVE MONITOR
        SafetyActionCard(
          title = "GUARDIAN CIRCLES & NIGHT TIMER",
          subtitle = "Live peer safety monitor, battery health & interval check-in",
          icon = Icons.Default.Group,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToGuardianCircle,
          testTag = "btn_guardian_circles"
        )

        // EMERGENCY SIREN & DEFENSE STROBE
        SafetyActionCard(
          title = "EMERGENCY SIREN & STROBE",
          subtitle = "High-decibel acoustic whistle, police siren & disorienting 8Hz strobe",
          icon = Icons.Default.VolumeUp,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToSirenStrobe,
          testTag = "btn_siren_strobe"
        )

        // RICKSHAW & PUBLIC TRANSIT GUARD
        SafetyActionCard(
          title = "RICKSHAW & TRANSIT GUARD",
          subtitle = "Vehicle plate logger, ride timer & route deviation detection",
          icon = Icons.Default.DirectionsBus,
          accentColor = MaterialTheme.colorScheme.primary,
          onClick = onNavigateToRideSafety,
          testTag = "btn_ride_safety"
        )

        // CAMPUS SAFETY & ESCORT DESK
        SafetyActionCard(
          title = "CAMPUS SAFETY & ESCORT",
          subtitle = "University safe corridors, proctor direct desk & volunteer escort",
          icon = Icons.Default.School,
          accentColor = SafeGreenPrimary,
          onClick = onNavigateToCampusSafety,
          testTag = "btn_campus_safety"
        )

        // Row for SELF DEFENSE and CAMOUFLAGE CALCULATOR
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SafetyMiniCard(
            title = "SELF-DEFENSE",
            subtitle = "Escape Maneuvers",
            icon = Icons.Default.SportsMartialArts,
            onClick = onNavigateToSelfDefense,
            modifier = Modifier.weight(1f),
            testTag = "btn_self_defense"
          )

          SafetyMiniCard(
            title = "STEALTH CALC",
            subtitle = "Camouflage SOS",
            icon = Icons.Default.Calculate,
            onClick = onNavigateToStealthMode,
            modifier = Modifier.weight(1f),
            testTag = "btn_stealth_calc"
          )
        }

        // Row for SAFETY VAULT and INCIDENT LOGS
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SafetyMiniCard(
            title = "SAFETY VAULT",
            subtitle = "Encrypted Audio",
            icon = Icons.Default.Lock,
            onClick = onNavigateToVault,
            modifier = Modifier.weight(1f),
            testTag = "btn_vault"
          )

          SafetyMiniCard(
            title = "INCIDENT LOGS",
            subtitle = "Beacon History",
            icon = Icons.Default.History,
            onClick = onNavigateToHistory,
            modifier = Modifier.weight(1f),
            testTag = "btn_history"
          )
        }

        // Row for LEGAL GUIDE and CYBER SUPPORT
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SafetyMiniCard(
            title = "LEGAL GUIDE",
            subtitle = "Rights & Protocols",
            icon = Icons.Default.AutoStories,
            onClick = onNavigateToSafetyGuide,
            modifier = Modifier.weight(1f),
            testTag = "btn_safety_guide"
          )

          SafetyMiniCard(
            title = "CYBER DESK (PCSW)",
            subtitle = "Police Cyber Unit",
            icon = Icons.Default.SupportAgent,
            onClick = onNavigateToCyberCrimeSupport,
            modifier = Modifier.weight(1f),
            testTag = "btn_cyber_desk"
          )
        }

        // Row for WELLNESS & TRAUMA and SETTINGS
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SafetyMiniCard(
            title = "WELLNESS & CARE",
            subtitle = "Trauma & Helplines",
            icon = Icons.Default.Spa,
            onClick = onNavigateToWellness,
            modifier = Modifier.weight(1f),
            testTag = "btn_wellness_care"
          )

          SafetyMiniCard(
            title = "SETTINGS",
            subtitle = "PIN & Preferences",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f),
            testTag = "btn_settings"
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Bangladesh Helpline Direct Bar
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hotline_info_bar"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Women & Child Helpline: 109",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
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
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.12f),
        modifier = Modifier.size(44.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
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
      .testTag(testTag)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
