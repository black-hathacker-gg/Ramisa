package com.example.features.defense

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfDefenseScreen(
  viewModel: SelfDefenseViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Practical Self-Defense",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Emergency escape maneuvers & physical security (আত্মরক্ষা)",
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

      // Header overview banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PinkPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.SportsMartialArts, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "SURVIVAL & ESCAPE PRINCIPLES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = PinkPrimaryDark
              )
              Text(
                text = "Target vulnerable anatomy, create space, and escape immediately towards illuminated safety zones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Legal Rights Badge
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Protected under Bangladesh Penal Code Sec 96-106 (Right of Private Defence).",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // MANEUVERS LIST HEADER
      item {
        Text(
          text = "CORE DEFENSIVE MANEUVERS",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // LIST OF MOVES
      items(uiState.moves) { move ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectMove(move) }
            .testTag("defense_card_${move.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PinkContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = move.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = move.scenario,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "View Steps", tint = PinkPrimary)
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }

  // Detail Modal Dialog for Selected Move
  uiState.selectedMove?.let { move ->
    AlertDialog(
      onDismissRequest = { viewModel.selectMove(null) },
      title = {
        Column {
          Text(
            text = move.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = move.titleBengali,
            style = MaterialTheme.typography.bodySmall,
            color = PinkPrimary
          )
        }
      },
      text = {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PinkContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "SCENARIO",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark
                )
                Text(
                  text = move.scenario,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          item {
            Text(
              text = "STEP-BY-STEP EXECUTION:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimary
            )
          }

          items(move.steps) { step ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = step,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          item {
            Text(
              text = "Primary Target Areas: ${move.targetAreas}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimaryDark
            )
            Text(
              text = "Legal: ${move.legalContext}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.selectMove(null) },
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
          modifier = Modifier.testTag("btn_close_defense_dialog")
        ) {
          Text("Got It")
        }
      }
    )
  }
}
