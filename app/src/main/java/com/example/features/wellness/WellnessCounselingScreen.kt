package com.example.features.wellness

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
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
fun WellnessCounselingScreen(
  viewModel: WellnessViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  val animatedScale by animateFloatAsState(
    targetValue = if (uiState.isBreathingActive) {
      if (uiState.breathingPhase.contains("Inhale") || uiState.breathingPhase.contains("Hold")) 1.25f else 0.85f
    } else 1.0f,
    animationSpec = tween(durationMillis = 3800),
    label = "breath_scale"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Trauma Recovery & Helplines",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Mental wellness, panic de-escalation & counseling (মানসিক স্বাস্থ্য)",
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
          containerColor = MaterialTheme.colorScheme.background
        )
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

      // Notification banner
      item {
        AnimatedVisibility(visible = uiState.feedbackMessage != null) {
          uiState.feedbackMessage?.let { msg ->
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
                TextButton(onClick = { viewModel.clearFeedback() }) {
                  Text("Dismiss", color = PinkPrimary)
                }
              }
            }
          }
        }
      }

      // PANIC & ANXIETY BOX BREATHING ENGINE
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_breathing_wellness"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "PANIC DE-ESCALATION (বক্স ব্রিদিং)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = PinkPrimaryDark
            )

            Box(
              modifier = Modifier
                .size(110.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(PinkPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
              )
            }

            Text(
              text = uiState.breathingPhase,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
              color = PinkPrimaryDark,
              textAlign = TextAlign.Center
            )

            Button(
              onClick = { viewModel.toggleBreathing() },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("btn_toggle_breathing")
            ) {
              Text(
                text = if (uiState.isBreathingActive) "Stop Breathing Loop" else "Start 4-4-4 Calming Breath",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // 5-4-3-2-1 GROUNDING TECHNIQUE
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "5-4-3-2-1 GROUNDING EXERCISE FOR TRAUMA",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
              color = PinkPrimary
            )

            val groundingPrompt = when (uiState.groundingTechniqueStep) {
              1 -> "Step 1: Look around and name 5 things you can SEE right now."
              2 -> "Step 2: Notice 4 things you can physically TOUCH or FEEL."
              3 -> "Step 3: Listen closely and identify 3 distinct sounds you can HEAR."
              4 -> "Step 4: Notice 2 things you can SMELL in your immediate surrounding."
              5 -> "Step 5: Notice 1 thing you can TASTE or take a deep sip of cold water."
              else -> "Repeat anytime you experience anxiety or trauma flashbacks."
            }

            Text(
              text = groundingPrompt,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = { viewModel.nextGroundingStep() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_next_grounding")
              ) {
                Text("Next Grounding Step (${uiState.groundingTechniqueStep}/5)", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }

      // BANGLADESH EMOTIONAL & TRAUMA HELPLINES
      item {
        Text(
          text = "CONFIDENTIAL PSYCHOSOCIAL COUNSELING",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.helplines) { item ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = item.organization,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Hours: ${item.available}",
                style = MaterialTheme.typography.labelSmall,
                color = PinkPrimary
              )
            }

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Call", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}
