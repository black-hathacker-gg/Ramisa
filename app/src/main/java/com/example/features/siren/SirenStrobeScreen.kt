package com.example.features.siren

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SirenStrobeScreen(
  viewModel: SirenStrobeViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  // Screen strobe background flash
  val strobeColor by animateColorAsState(
    targetValue = if (uiState.isStrobeActive) {
      if (uiState.strobeColorIndex == 0) Color.White else PinkPrimary
    } else {
      MaterialTheme.colorScheme.background
    },
    label = "strobe_color"
  )

  // Infinite pulsing scale for siren button
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (uiState.isSirenPlaying) 1.12f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Emergency Siren & Strobe",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Acoustic & visual deterrent tool (সাইরেন ও আলো)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = if (uiState.isStrobeActive) strobeColor else MaterialTheme.colorScheme.background
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = strobeColor
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // Notification banner
      AnimatedVisibility(visible = uiState.statusMessage != null) {
        uiState.statusMessage?.let { msg ->
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = PinkContainer
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimaryDark
              )
              TextButton(onClick = { viewModel.clearStatusMessage() }) {
                Text("Dismiss", color = PinkPrimary)
              }
            }
          }
        }
      }

      // BIG MASTER DETERRENT BUTTON
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_master_defense"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (uiState.isSirenPlaying || uiState.isStrobeActive) PinkPrimary else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(120.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(if (uiState.isSirenPlaying || uiState.isStrobeActive) Color.White else PinkPrimary)
              .clickable { viewModel.toggleAllDefense() }
              .testTag("btn_master_defense_toggle"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (uiState.isSirenPlaying || uiState.isStrobeActive) Icons.Default.Warning else Icons.Default.VolumeUp,
              contentDescription = "Master Defense Toggle",
              tint = if (uiState.isSirenPlaying || uiState.isStrobeActive) PinkPrimary else Color.White,
              modifier = Modifier.size(54.dp)
            )
          }

          Text(
            text = if (uiState.isSirenPlaying || uiState.isStrobeActive) "ALL DEFENSE SYSTEMS ACTIVE" else "TAP FOR MAXIMUM DETERRENT",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = if (uiState.isSirenPlaying || uiState.isStrobeActive) Color.White else PinkPrimaryDark,
            textAlign = TextAlign.Center
          )

          Text(
            text = "Triggers max-decibel siren and high-frequency visual flash to disorient attackers & summon help immediately.",
            style = MaterialTheme.typography.bodySmall,
            color = if (uiState.isSirenPlaying || uiState.isStrobeActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }

      // INDIVIDUAL CONTROLS (SIREN & STROBE)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Siren Toggle Card
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("card_siren_control"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isSirenPlaying) PinkContainer else MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (uiState.isSirenPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(24.dp)
              )
              Switch(
                checked = uiState.isSirenPlaying,
                onCheckedChange = { viewModel.toggleSiren() },
                modifier = Modifier.testTag("switch_siren")
              )
            }

            Text(
              text = "Sonic Siren",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = if (uiState.isSirenPlaying) "Playing high-pitch audio" else "Muted",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Strobe Toggle Card
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("card_strobe_control"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isStrobeActive) PinkContainer else MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(24.dp)
              )
              Switch(
                checked = uiState.isStrobeActive,
                onCheckedChange = { viewModel.toggleStrobe() },
                modifier = Modifier.testTag("switch_strobe")
              )
            }

            Text(
              text = "Strobe Light",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = if (uiState.isStrobeActive) "Flashing at 8Hz" else "Inactive",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // SIREN FREQUENCY & PROFILE SELECTOR
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_siren_modes"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "SELECT DETERRENT SOUND PATTERN",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = PinkPrimary
          )

          SirenMode.values().forEach { mode ->
            val isSelected = uiState.activeMode == mode
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.setMode(mode) }
                .testTag("siren_mode_${mode.name}"),
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) PinkContainer else Color.Transparent,
              border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PinkPrimary else MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = mode.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PinkPrimaryDark else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = mode.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
