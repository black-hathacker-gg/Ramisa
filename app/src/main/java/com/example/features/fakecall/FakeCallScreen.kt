package com.example.features.fakecall

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import com.example.domain.model.FakeCallerProfile
import com.example.ui.theme.PinkBorderSubtle
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryLight
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeCallScreen(
  viewModel: FakeCallViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  BackHandler(enabled = uiState.state == FakeCallState.RINGING || uiState.state == FakeCallState.CONNECTED) {
    viewModel.declineOrEndCall()
  }

  when (uiState.state) {
    FakeCallState.RINGING -> {
      IncomingCallUi(
        profile = uiState.selectedProfile,
        onAccept = { viewModel.acceptCall() },
        onDecline = { viewModel.declineOrEndCall() }
      )
    }

    FakeCallState.CONNECTED -> {
      ConnectedCallUi(
        profile = uiState.selectedProfile,
        durationSeconds = uiState.callDurationSeconds,
        onEndCall = { viewModel.declineOrEndCall() }
      )
    }

    else -> {
      // Configuration & Countdown view
      val scrollState = rememberScrollState()

      Scaffold(
        topBar = {
          TopAppBar(
            title = {
              Text(
                text = "Fake call",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = Color.White,
              navigationIconContentColor = Color.White,
              actionIconContentColor = Color.White
            )
          )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
      ) { paddingValues ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Discreet Safety Escape",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Pick a caller and a delay. RAMISA plays the device ringtone and displays a pretend call.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
          )

          if (uiState.state == FakeCallState.COUNTDOWN) {
            // Active Countdown Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("countdown_card"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "INCOMING CALL IN",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "${uiState.countdownRemaining}s",
                  style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "From: ${uiState.selectedProfile.name}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                  onClick = { viewModel.cancelSchedule() },
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("Cancel Timer")
                }
              }
            }

            Spacer(modifier = Modifier.height(24.dp))
          }

          // Choose Caller Profile
          Text(
            text = "CHOOSE CALLER PROFILE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            uiState.profiles.forEach { profile ->
              val isSelected = uiState.selectedProfile.id == profile.id
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { viewModel.selectProfile(profile) }
                  .testTag("profile_card_${profile.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = profile.name,
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${profile.relation} • ${profile.phoneNumber}",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Select Timer Delay
          Text(
            text = "TRIGGER DELAY (টাইমার)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(0 to "Now", 5 to "5s", 15 to "15s", 30 to "30s", 60 to "1m").forEach { (secs, label) ->
              FilterChip(
                selected = uiState.delaySeconds == secs,
                onClick = { viewModel.setDelaySeconds(secs) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("delay_chip_$secs")
              )
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          // Trigger Button
          Button(
            onClick = { viewModel.triggerCallSchedule() },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("btn_trigger_fake_call"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (uiState.delaySeconds == 0) "CALL ME RIGHT NOW" else "SCHEDULE CALL (${uiState.delaySeconds}s)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}


@Composable
private fun IncomingCallUi(
  profile: FakeCallerProfile,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    var ringtone: Ringtone? = null
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    try {
      val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      ringtone = RingtoneManager.getRingtone(context, uri)
      ringtone?.play()
      
      val pattern = longArrayOf(0, 1000, 1000)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, 0)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    onDispose {
      ringtone?.stop()
      vibrator?.cancel()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(SurfaceLight, PinkContainer)))
      .padding(32.dp)
      .testTag("incoming_call_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top caller info
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
      ) {
        Text(
          text = "INCOMING CALL",
          style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
          color = PinkPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Pulse Ring
        Box(
          modifier = Modifier.size(130.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(130.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(PinkPrimaryLight.copy(alpha = 0.35f))
          )
          Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, PinkBorderSubtle)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(54.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = profile.name,
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = profile.phoneNumber,
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondaryLight
        )
      }

      // Action Buttons (Decline / Accept)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Decline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Surface(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .clickable(onClick = onDecline)
              .testTag("btn_decline_fake_call"),
            shape = CircleShape,
            color = Color(0xFFE53935),
            shadowElevation = 4.dp
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(30.dp))
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text("Decline", color = TextSecondaryLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        // Accept
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Surface(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .clickable(onClick = onAccept)
              .testTag("btn_accept_fake_call"),
            shape = CircleShape,
            color = PinkPrimary,
            shadowElevation = 4.dp
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Call, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(30.dp))
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text("Accept", color = TextSecondaryLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun ConnectedCallUi(
  profile: FakeCallerProfile,
  durationSeconds: Int,
  onEndCall: () -> Unit
) {
  val mins = durationSeconds / 60
  val secs = durationSeconds % 60
  val durationFormatted = String.format("%02d:%02d", mins, secs)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(SurfaceLight, PinkContainer)))
      .padding(32.dp)
      .testTag("connected_call_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
      ) {
        Text(
          text = profile.name,
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = durationFormatted,
          style = MaterialTheme.typography.titleMedium,
          color = PinkPrimary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Audio Prompt / Speaking Script Card (Guidance for User)
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, PinkBorderSubtle),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Suggested script",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimary
            )
            Text(
              text = profile.defaultScriptBn,
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimaryLight
            )
            Text(
              text = profile.defaultScriptEn,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight
            )
          }
        }
      }

      // Call Controls
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 32.dp)
      ) {
        // End Call
        Surface(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onEndCall)
            .testTag("btn_end_fake_call"),
          shape = CircleShape,
          color = Color(0xFFE53935),
          shadowElevation = 4.dp
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(30.dp))
          }
        }
      }
    }
  }
}
