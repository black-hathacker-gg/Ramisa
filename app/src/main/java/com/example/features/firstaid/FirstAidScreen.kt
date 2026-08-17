package com.example.features.firstaid

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstAidScreen(
  viewModel: FirstAidViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  val filteredTopics = if (uiState.searchQuery.isBlank()) {
    uiState.topics
  } else {
    uiState.topics.filter {
      it.title.contains(uiState.searchQuery, ignoreCase = true) ||
      it.titleBengali.contains(uiState.searchQuery, ignoreCase = true) ||
      it.category.contains(uiState.searchQuery, ignoreCase = true)
    }
  }

  // MODAL FOR SELECTED TOPIC
  if (uiState.selectedTopic != null) {
    val topic = uiState.selectedTopic!!
    AlertDialog(
      onDismissRequest = { viewModel.selectTopic(null) },
      title = {
        Column {
          Text(
            text = topic.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = topic.titleBengali,
            style = MaterialTheme.typography.bodySmall,
            color = PinkPrimary
          )
        }
      },
      text = {
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PinkContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "QUICK ACTION: ${topic.quickAction}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimaryDark,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          item {
            Text(
              text = "STEP-BY-STEP PROCEDURE",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimary
            )
          }

          items(topic.steps) { step ->
            Text(
              text = step,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(
              text = "CRITICAL WARNINGS",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFFD32F2F)
            )
          }

          items(topic.warnings) { warn ->
            Text(
              text = "⚠️ $warn",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFD32F2F)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.selectTopic(null) },
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
        ) {
          Text("Close Guide")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Offline First Aid & Triage",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Emergency medical, trauma & chemical triage (ফার্স্ট এইড)",
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

      // AMBULANCE SPEED CALL
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_first_aid_emergency_call"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(PinkPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color.White)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "EMERGENCY AMBULANCE",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                  color = PinkPrimaryDark
                )
                Text(
                  text = "Dial 999 / 199 Ambulance",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("btn_call_ambulance")
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("999", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // SEARCH BOX
      item {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = { viewModel.updateSearchQuery(it) },
          placeholder = { Text("Search first-aid (e.g. bleeding, pepper spray)...") },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          modifier = Modifier.fillMaxWidth().testTag("input_first_aid_search")
        )
      }

      // TOPICS LIST
      item {
        Text(
          text = "OFFLINE TRIAGE & FIRST AID PROTOCOLS",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(filteredTopics) { topic ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectTopic(topic) },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = topic.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = topic.titleBengali,
                  style = MaterialTheme.typography.bodySmall,
                  color = PinkPrimary
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (topic.urgencyLevel == "CRITICAL") Color(0xFFFFEBEE) else PinkContainer
              ) {
                Text(
                  text = topic.urgencyLevel,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (topic.urgencyLevel == "CRITICAL") Color(0xFFC62828) else PinkPrimaryDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Text(
              text = topic.quickAction,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { viewModel.selectTopic(topic) }) {
                Text("View Full Steps ➔", color = PinkPrimary, fontWeight = FontWeight.Bold)
              }
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
