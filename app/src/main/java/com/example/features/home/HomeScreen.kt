package com.example.features.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.community.CommunityManager
import com.example.data.repository.AppSessionManager
import com.example.ui.theme.*

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

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(bottom = 100.dp, top = 48.dp) // Padding for bottom nav and status bar
  ) {
    // TOP: Greeting & Status
    HomeHeaderSection()

    // Inspirational Quote
    QuoteBanner()

    // MAIN: Large premium pink SOS safety orb
    Spacer(modifier = Modifier.height(32.dp))
    SosOrbSection(onTriggerSos = onTriggerSos)
    
    // SAFETY STATUS
    Spacer(modifier = Modifier.height(32.dp))
    SafetyStatusBanner()

    // SAFETY CIRCLE
    Spacer(modifier = Modifier.height(24.dp))
    SafetyCircleSection(onNavigateToContacts = onNavigateToContacts)

    // YOUR SAFETY (2x2 Grid)
    Spacer(modifier = Modifier.height(24.dp))
    YourSafetySection(
      onNavigateToSafeJourney = onNavigateToSafeJourney,
      onNavigateToDangerMap = onNavigateToDangerMap,
      onNavigateToHistory = onNavigateToHistory,
      onNavigateToSafePlaces = onNavigateToSafePlaces
    )
  }
}

@Composable
fun HomeHeaderSection() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "RAMISA",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
        color = PinkPrimary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Good evening,",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "You're protected",
        style = MaterialTheme.typography.bodyLarge,
        color = PinkPrimaryDark
      )
    }
    
    // Placeholder Avatar
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(PinkContainer)
        .border(2.dp, PinkPrimaryLight, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Person, contentDescription = "Profile", tint = PinkPrimary)
    }
  }
}

@Composable
fun QuoteBanner() {
  Spacer(modifier = Modifier.height(16.dp))
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape = RoundedCornerShape(16.dp),
    color = PinkContainer.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, PinkPrimary.copy(alpha = 0.1f))
  ) {
    Text(
      text = "\"Please don't stop flying, fly freely to your desired sky\"",
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
      ),
      color = PinkPrimaryDark,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(16.dp)
    )
  }
}


@Composable
fun SosOrbSection(onTriggerSos: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition()
  val idleScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    )
  )

  var isHolding by remember { mutableStateOf(false) }
  var holdProgress by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(isHolding) {
    if (isHolding) {
      val totalTime = 3000 // 3 seconds
      val stepTime = 30
      var elapsed = 0
      while (elapsed < totalTime) {
        delay(stepTime.toLong())
        elapsed += stepTime
        holdProgress = elapsed.toFloat() / totalTime
      }
      holdProgress = 1f
      onTriggerSos()
      isHolding = false
      holdProgress = 0f
    } else {
      holdProgress = 0f
    }
  }

  val orbScale = if (isHolding) 0.95f else idleScale

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(240.dp)
        .scale(orbScale)
        .clip(CircleShape)
        .background(PinkContainer.copy(alpha = 0.5f))
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              isHolding = true
              try {
                awaitRelease()
              } finally {
                isHolding = false
              }
            }
          )
        },
      contentAlignment = Alignment.Center
    ) {
      // Progress Ring
      if (holdProgress > 0f) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
          drawArc(
            color = PinkPrimaryDark,
            startAngle = -90f,
            sweepAngle = 360f * holdProgress,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
          )
        }
      }

      Box(
        modifier = Modifier
          .size(200.dp)
          .clip(CircleShape)
          .background(Brush.radialGradient(listOf(if (isHolding) PinkPrimaryDark else PinkPrimary, PinkPrimaryDark))),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = if (isHolding) {
              val remaining = 3 - (holdProgress * 3).toInt()
              remaining.toString()
            } else "SOS",
            style = MaterialTheme.typography.displayLarge.copy(color = Color.White)
          )
          Text(
            text = if (isHolding) "ACTIVATING..." else "HOLD 3 SEC",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
          )
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Emergency ready",
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = PinkPrimary
    )
  }
}

@Composable
fun SafetyStatusBanner() {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape = RoundedCornerShape(24.dp),
    color = SurfaceVariantLight,
    border = BorderStroke(1.dp, PinkBorderSubtle)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier.size(12.dp).clip(CircleShape).background(PinkPrimary)
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(text = "YOU'RE SAFE", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PinkPrimaryDark)
        Text(text = "No active alerts", style = MaterialTheme.typography.bodySmall, color = PinkPrimary)
      }
    }
  }
}

@Composable
fun SafetyCircleSection(onNavigateToContacts: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "YOUR SAFETY CIRCLE",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(horizontal = 24.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    LazyRow(
      contentPadding = PaddingValues(horizontal = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      val contacts = listOf("Mom", "Sister", "Friend")
      items(contacts.size) { index ->
        ContactAvatar(name = contacts[index])
      }
      item {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToContacts() }) {
          Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceVariantLight).border(1.dp, PinkBorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = PinkPrimary)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Add", style = MaterialTheme.typography.bodySmall, color = PinkPrimary)
        }
      }
    }
  }
}

@Composable
fun ContactAvatar(name: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier.size(64.dp).clip(CircleShape).background(PinkContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(text = name.first().toString(), style = MaterialTheme.typography.titleLarge, color = PinkPrimaryDark)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
  }
}

@Composable
fun YourSafetySection(
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToDangerMap: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToSafePlaces: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
    Text(
      text = "YOUR SAFETY",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      SafetyActionCard(title = "Safe Journey", icon = Icons.Default.DirectionsWalk, onClick = onNavigateToSafeJourney, modifier = Modifier.weight(1f))
      SafetyActionCard(title = "Map", icon = Icons.Default.Map, onClick = onNavigateToDangerMap, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      SafetyActionCard(title = "Evidence", icon = Icons.Default.FolderSpecial, onClick = onNavigateToHistory, modifier = Modifier.weight(1f))
      SafetyActionCard(title = "Safe Places", icon = Icons.Default.LocalHospital, onClick = onNavigateToSafePlaces, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
fun SafetyActionCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(20.dp),
    color = SurfaceVariantLight,
    border = BorderStroke(1.dp, PinkBorderSubtle)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = PinkPrimary, modifier = Modifier.size(28.dp))
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PinkPrimaryDark)
    }
  }
}
