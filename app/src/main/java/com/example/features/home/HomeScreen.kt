package com.example.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Key
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
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.community.CommunityManager
import com.example.data.repository.AppSessionManager
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

enum class SafetyCategoryTab(val labelEn: String, val labelBn: String, val icon: ImageVector) {
  OVERVIEW("Overview", "হোম", Icons.Default.Shield),
  EMERGENCY("Emergency", "জরুরি", Icons.Default.Lock),
  NAVIGATION("Navigation", "ম্যাপ ও যাত্রা", Icons.Default.DirectionsWalk),
  SENSORS("Shield & AI", "সেন্সর ও এআই", Icons.Default.Sensors),
  COMMUNITY("Care & Legal", "সহায়তা ও আইন", Icons.Default.Group)
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
  onNavigateToStealthMode: () -> Unit = {},
  onNavigateToSirenStrobe: () -> Unit = {},
  onNavigateToRideSafety: () -> Unit = {},
  onNavigateToSelfDefense: () -> Unit = {},
  onNavigateToCyberCrimeSupport: () -> Unit = {},
  onNavigateToWellness: () -> Unit = {},
  onNavigateToVolunteerEscort: () -> Unit = {},
  onNavigateToLegalAid: () -> Unit = {},
  onNavigateToSafetyTimer: () -> Unit = {},
  onNavigateToFirstAid: () -> Unit = {},
  onNavigateToLockScreenSos: () -> Unit = {},
  onNavigateToDangerMap: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsState()
  val communities by CommunityManager.communities.collectAsState()
  val activeSosAlerts by CommunityManager.activeSosAlerts.collectAsState()
  val primaryCommunity = communities.firstOrNull { it.isJoined } ?: communities.firstOrNull()
  val activeCommunitySosCount = activeSosAlerts.count { it.status != "RESOLVED" }
  val latestSosAlert = activeSosAlerts.firstOrNull { it.status != "RESOLVED" }

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
        .fillMaxWidth()
        .statusBarsPadding()
        .navigationBarsPadding()
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
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(Color.White)
                  .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_app_logo),
                  contentDescription = "RAMISA Official Logo",
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                )
              }
              Column(
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = "RAMISA",
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                  ),
                  color = Color.White,
                  maxLines = 1,
                  modifier = Modifier.testTag("app_logo_title")
                )
                Text(
                  text = if (isBangla) "নারী ও শিশুর ব্যক্তিগত নিরাপত্তা" else "Women & Child Personal Safety",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                  color = Color.White.copy(alpha = 0.92f),
                  maxLines = 1
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Language Switcher Toggle (বাং / EN)
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                  .clickable { AppSessionManager.toggleLanguage() }
                  .testTag("btn_language_toggle")
              ) {
                Text(
                  text = if (isBangla) "বাং" else "EN",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
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

      // 2. STATUS BANNER & 3X SHAKE SOS INDICATOR
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("safety_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PinkContainer),
        border = BorderStroke(1.dp, PinkBorderSubtle)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {


          // Shake 3x Indicator Info
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.7f),
            border = BorderStroke(0.5.dp, PinkBorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = "Shake Detection",
                tint = PinkPrimaryDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isBangla) "ফোন ৩ বার ঝাঁকালে (Shake) অটোমেটিক SOS ও লোকেশন SMS যাবে" 
                       else "Shake device 3x to auto trigger SOS & Fused GPS SMS",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                ),
                color = PinkPrimaryDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. CENTRAL SOS BUTTON (Large, Distinct Pink Floating Action Button - SOS - SMS & CALL GUARDIANS)
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
                text = if (isBangla) "অভিভাবকদের SMS ও কল" else "SMS & CALL GUARDIANS",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 1.2.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // COMMUNITY GUARD NETWORK STATUS BANNER
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onNavigateToCommunitySafety)
          .testTag("card_community_network_home"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (activeCommunitySosCount > 0) SafetyRedPrimary.copy(alpha = 0.12f) else PinkAccentLilac.copy(alpha = 0.7f)
        ),
        border = BorderStroke(
          1.5.dp,
          if (activeCommunitySosCount > 0) SafetyRedPrimary else PinkBorderSubtle
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (activeCommunitySosCount > 0) SafetyRedPrimary else MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (activeCommunitySosCount > 0) Icons.Default.Warning else Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              if (activeCommunitySosCount > 0 && latestSosAlert != null) {
                Text(
                  text = if (isBangla) "জরুরি অ্যালার্ট: ${latestSosAlert.victimName}" else "CRISIS: ${latestSosAlert.victimName} SOS!",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = SafetyRedDark)
                )
                Text(
                  text = "${latestSosAlert.triggerTypeLabel} • ${latestSosAlert.locationName}",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                  color = SafetyRedDark,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              } else {
                Text(
                  text = if (isBangla) "রমিসা সেন্ট্রাল নারী ফোরাম ও সেফটি" else "RAMISA Central Women's Forum & Safety",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PinkPrimaryDark)
                )
                Text(
                  text = if (isBangla) "৩,৪৫০ বোন সংযুক্ত • ফোরাম আলোচনা ও অটোমেটিক SOS" else "3,450 Sisters • Messenger Forum & Auto SOS",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          Surface(
            color = if (activeCommunitySosCount > 0) SafetyRedPrimary else MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = if (activeCommunitySosCount > 0) (if (isBangla) "রেসপন্ড" else "RESPOND") else (if (isBangla) "ফোরাম" else "FORUM"),
              color = Color.White,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. IMMEDIATE EMERGENCY SHORTCUTS (4 Core Priority Tools)
      Text(
        text = if (isBangla) "জরুরি কুইক টুলস" else "PRIORITY EMERGENCY CONTROLS",
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
          title = if (isBangla) "লকস্ক্রিন SOS" else "LockScreen SOS",
          icon = Icons.Default.Lock,
          accentColor = SafetyRedPrimary,
          onClick = onNavigateToLockScreenSos,
          modifier = Modifier.weight(1f),
          testTag = "btn_lockscreen_sos_guard"
        )
        QuickEmergencyPill(
          title = if (isBangla) "ফেক কল" else "Fake Call",
          icon = Icons.Default.PhoneCallback,
          accentColor = PinkPrimary,
          onClick = onNavigateToFakeCall,
          modifier = Modifier.weight(1f),
          testTag = "btn_fake_call"
        )
        QuickEmergencyPill(
          title = if (isBangla) "সাইরেন ও লাইট" else "Siren / Strobe",
          icon = Icons.Default.VolumeUp,
          accentColor = PinkPrimary,
          onClick = onNavigateToSirenStrobe,
          modifier = Modifier.weight(1f),
          testTag = "btn_siren_strobe"
        )
        QuickEmergencyPill(
          title = if (isBangla) "নিরাপদ যাত্রা" else "Safe Journey",
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
                text = if (isBangla) tab.labelBn else tab.labelEn,
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
            onNavigateToHardwareTrigger = onNavigateToHardwareTrigger,
            onNavigateToMeshNetwork = onNavigateToMeshNetwork,
            onNavigateToRideSafety = onNavigateToRideSafety,
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
            onNavigateToSafetyTimer = onNavigateToSafetyTimer
          )
        }
        SafetyCategoryTab.COMMUNITY -> {
          CommunityTabSection(
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
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Helpline",
              tint = PinkPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (isBangla) "জরুরি হটলাইন সেবা (টোল ফ্রি)" else "National Emergency Hotlines",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimaryDark
              )
              Text(
                text = "999 (জাতীয় জরুরি) • 109 (নারী সহায়তা) • 1098 (শিশু সহায়তা)",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun QuickEmergencyPill(
  title: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Card(
    modifier = modifier
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, PinkBorderSubtle)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(PinkContainer, PinkAccentLilac)
            )
          )
          .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accentColor,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

// ---------------------- TAB SECTIONS ----------------------

@Composable
fun OverviewSection(
  onNavigateToContacts: () -> Unit,
  onNavigateToSafePlaces: () -> Unit,
  onNavigateToDangerMap: () -> Unit,
  onNavigateToThreatGuard: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToMeshNetwork: () -> Unit,
  onNavigateToRideSafety: () -> Unit,
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
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Emergency Contacts",
        subtitle = "Manage trusted guardians & SMS targets",
        icon = Icons.Default.Contacts,
        onClick = onNavigateToContacts,
        modifier = Modifier.weight(1f),
        testTag = "card_contacts"
      )
      FeatureCard(
        title = "Safe Journey & Ride",
        subtitle = "Live route tracking & destination check-in",
        icon = Icons.Default.DirectionsWalk,
        onClick = onNavigateToSafeJourney,
        modifier = Modifier.weight(1f),
        testTag = "card_safe_journey"
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Danger Map & Shelters",
        subtitle = "Reported zones & nearest police stations",
        icon = Icons.Default.Place,
        onClick = onNavigateToDangerMap,
        modifier = Modifier.weight(1f),
        testTag = "card_danger_map"
      )
      FeatureCard(
        title = "AI Threat Guard",
        subtitle = "Speech & distress sound analysis",
        icon = Icons.Default.Sensors,
        onClick = onNavigateToThreatGuard,
        modifier = Modifier.weight(1f),
        testTag = "card_threat_guard"
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "AI Threat Guard",
        subtitle = "Speech & distress sound analysis",
        icon = Icons.Default.Sensors,
        onClick = onNavigateToThreatGuard,
        modifier = Modifier.weight(1f),
        testTag = "card_threat_guard"
      )
      FeatureCard(
        title = "Safety Timer",
        subtitle = "Dead Man check-in timer",
        icon = Icons.Default.Timer,
        onClick = onNavigateToSafetyTimer,
        modifier = Modifier.weight(1f),
        testTag = "card_safety_timer"
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "App Settings & Hardware",
        subtitle = "Power button triggers & preferences",
        icon = Icons.Default.Settings,
        onClick = onNavigateToSettings,
        modifier = Modifier.weight(1f),
        testTag = "card_settings"
      )
    }
  }
}

@Composable
fun EmergencyTabSection(
  onNavigateToLockScreenSos: () -> Unit,
  onNavigateToFakeCall: () -> Unit,
  onNavigateToSirenStrobe: () -> Unit,
  onNavigateToContacts: () -> Unit,
  onNavigateToMeshNetwork: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    FeatureCard(
      title = "Lock Screen SOS Guard",
      subtitle = "Instant alert button on Android lock screen & notification tray",
      icon = Icons.Default.Lock,
      onClick = onNavigateToLockScreenSos,
      testTag = "card_lockscreen_sos"
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Fake Incoming Call",
        subtitle = "Simulate rescue phone call",
        icon = Icons.Default.PhoneCallback,
        onClick = onNavigateToFakeCall,
        modifier = Modifier.weight(1f),
        testTag = "card_fake_call"
      )
      FeatureCard(
        title = "Siren & Strobe Light",
        subtitle = "105dB alarm & flashing light",
        icon = Icons.Default.VolumeUp,
        onClick = onNavigateToSirenStrobe,
        modifier = Modifier.weight(1f),
        testTag = "card_siren_strobe"
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Hardware Triggers",
        subtitle = "Triple power button & shake SOS",
        icon = Icons.Default.Sensors,
        onClick = onNavigateToHardwareTrigger,
        modifier = Modifier.weight(1f),
        testTag = "card_hardware_triggers"
      )
      FeatureCard(
        title = "Offline Mesh SOS",
        subtitle = "Bluetooth SOS without internet",
        icon = Icons.Default.CellTower,
        onClick = onNavigateToMeshNetwork,
        modifier = Modifier.weight(1f),
        testTag = "card_mesh_network"
      )
    }
    FeatureCard(
      title = "Dead Man's Safety Timer",
      subtitle = "Periodic check-in timer that alerts guardians if missed",
      icon = Icons.Default.Timer,
      onClick = onNavigateToSafetyTimer,
      testTag = "card_safety_timer"
    )
  }
}

@Composable
fun NavigationTabSection(
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToDangerMap: () -> Unit,
  onNavigateToSafePlaces: () -> Unit,
  onNavigateToRideSafety: () -> Unit,
  onNavigateToCommunitySafety: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    FeatureCard(
      title = "Safe Journey & Live Tracking",
      subtitle = "Monitor walk with auto ETA check-in & deviation alert",
      icon = Icons.Default.DirectionsWalk,
      onClick = onNavigateToSafeJourney,
      testTag = "card_safe_journey_nav"
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Danger Zones Map",
        subtitle = "Crowdsourced hazard hotspots",
        icon = Icons.Default.Place,
        onClick = onNavigateToDangerMap,
        modifier = Modifier.weight(1f),
        testTag = "card_danger_map_nav"
      )
      FeatureCard(
        title = "Safe Havens & Police",
        subtitle = "Hospitals, stations & shelters",
        icon = Icons.Default.LocalHospital,
        onClick = onNavigateToSafePlaces,
        modifier = Modifier.weight(1f),
        testTag = "card_safe_places_nav"
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Ride & Transport Safety",
        subtitle = "Share license plate & GPS link",
        icon = Icons.Default.DirectionsBus,
        onClick = onNavigateToRideSafety,
        modifier = Modifier.weight(1f),
        testTag = "card_ride_safety_nav"
      )
      FeatureCard(
        title = "Community Safety Reports",
        subtitle = "Verified local danger alerts",
        icon = Icons.Default.Group,
        onClick = onNavigateToCommunitySafety,
        modifier = Modifier.weight(1f),
        testTag = "card_community_safety_nav"
      )
    }
  }
}

@Composable
fun SensorsTabSection(
  onNavigateToThreatGuard: () -> Unit,
  onNavigateToHardwareTrigger: () -> Unit,
  onNavigateToStealthMode: () -> Unit,
  onNavigateToSafetyTimer: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    FeatureCard(
      title = "AI Distress & Screaming Audio Guard",
      subtitle = "Gemini AI background acoustic detection for screaming & distress words",
      icon = Icons.Default.Sensors,
      onClick = onNavigateToThreatGuard,
      testTag = "card_threat_guard_sensor"
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Stealth Calculator Camouflage",
        subtitle = "Hide app behind real calculator",
        icon = Icons.Default.Calculate,
        onClick = onNavigateToStealthMode,
        modifier = Modifier.weight(1f),
        testTag = "card_stealth_calc"
      )
      FeatureCard(
        title = "Hardware Accelerometer",
        subtitle = "3x Shake & Power button SOS",
        icon = Icons.Default.FlashOn,
        onClick = onNavigateToHardwareTrigger,
        modifier = Modifier.weight(1f),
        testTag = "card_hardware_sensor"
      )
    }

  }
}

@Composable
fun CommunityTabSection(
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
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {


    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Legal Aid & Rights",
        subtitle = "Bangladesh legal helpline 109 & 999",
        icon = Icons.Default.Gavel,
        onClick = onNavigateToLegalAid,
        modifier = Modifier.weight(1f),
        testTag = "card_legal_comm"
      )
      FeatureCard(
        title = "Cyber Crime Support",
        subtitle = "Police cyber crime unit contact",
        icon = Icons.Default.Security,
        onClick = onNavigateToCyberCrimeSupport,
        modifier = Modifier.weight(1f),
        testTag = "card_cyber_comm"
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureCard(
        title = "Self Defense Guides",
        subtitle = "Practical crisis escape maneuvers",
        icon = Icons.Default.SportsMartialArts,
        onClick = onNavigateToSelfDefense,
        modifier = Modifier.weight(1f),
        testTag = "card_defense_comm"
      )
      FeatureCard(
        title = "First Aid Tips",
        subtitle = "Offline emergency medical care",
        icon = Icons.Default.AutoStories,
        onClick = onNavigateToFirstAid,
        modifier = Modifier.weight(1f),
        testTag = "card_firstaid_comm"
      )
    }
  }
}

@Composable
fun FeatureCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, PinkBorderSubtle)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(PinkContainer, PinkAccentLilac)
            )
          )
          .border(1.5.dp, PinkPrimary.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = PinkPrimary,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
      }
    }
  }
}
