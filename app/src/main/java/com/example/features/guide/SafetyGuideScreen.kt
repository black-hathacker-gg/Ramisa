package com.example.features.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SafetyTip
import com.example.domain.model.TipCategory
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyGuideScreen(
  viewModel: SafetyGuideViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Safety & Legal Guide (নির্দেশিকা)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 18.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Knowledge & Legal Rights",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "Practical protocols, legal rights in Bangladesh, and self-defense awareness.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      // Category Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = uiState.selectedCategory == null,
          onClick = { viewModel.selectCategory(null) },
          label = { Text("All", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedCategory == TipCategory.LEGAL_RIGHTS,
          onClick = { viewModel.selectCategory(TipCategory.LEGAL_RIGHTS) },
          label = { Text("Legal Rights", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedCategory == TipCategory.TRANSIT,
          onClick = { viewModel.selectCategory(TipCategory.TRANSIT) },
          label = { Text("Transit", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedCategory == TipCategory.CYBER_SAFETY,
          onClick = { viewModel.selectCategory(TipCategory.CYBER_SAFETY) },
          label = { Text("Cyber Safety", fontSize = 11.sp) }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tips List
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(uiState.tips, key = { it.id }) { tip ->
          SafetyTipCard(
            tip = tip,
            isExpanded = uiState.expandedTipId == tip.id,
            onToggle = { viewModel.toggleExpand(tip.id) }
          )
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun SafetyTipCard(
  tip: SafetyTip,
  isExpanded: Boolean,
  onToggle: () -> Unit
) {
  val (icon: ImageVector, categoryLabel: String) = when (tip.category) {
    TipCategory.TRANSIT -> Icons.Default.DirectionsBus to "TRANSIT SAFETY"
    TipCategory.STREET_SAFETY -> Icons.Default.Security to "STREET SAFETY"
    TipCategory.LEGAL_RIGHTS -> Icons.Default.Gavel to "BD LEGAL ACT"
    TipCategory.CYBER_SAFETY -> Icons.Default.Lock to "CYBER SECURITY"
    TipCategory.FIRST_AID -> Icons.Default.HealthAndSafety to "FIRST AID"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onToggle)
      .testTag("safety_tip_card_${tip.id}"),
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
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = categoryLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.Top) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = tip.titleEn,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = tip.titleBn,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = tip.summaryEn,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Detailed Guidelines:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = tip.detailsEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "বাংলা নির্দেশিকা:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = tip.detailsBn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (tip.legalActRef != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Legal Reference: ${tip.legalActRef}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      }
    }
  }
}
