package com.example.features.campus

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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun CampusSafetyScreen(
  viewModel: CampusSafetyViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var expandedUniDropdown by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Campus Safety & Escort",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "University Proctor Desk & Safe Corridors (ক্যাম্পাস সুরক্ষা)",
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

      // University Selector Dropdown
      item {
        ExposedDropdownMenuBox(
          expanded = expandedUniDropdown,
          onExpandedChange = { expandedUniDropdown = !expandedUniDropdown },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = uiState.selectedUniversity,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Campus / University (বিশ্ববিদ্যালয়)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUniDropdown) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor()
              .testTag("dropdown_university")
          )

          ExposedDropdownMenu(
            expanded = expandedUniDropdown,
            onDismissRequest = { expandedUniDropdown = false }
          ) {
            uiState.universities.forEach { uni ->
              DropdownMenuItem(
                text = { Text(uni) },
                onClick = {
                  viewModel.selectUniversity(uni)
                  expandedUniDropdown = false
                }
              )
            }
          }
        }
      }

      // PROCTOR QUICK CONTACT CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_proctor_contact"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(PinkContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = PinkPrimary)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "PROCTOR EMERGENCIES",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                  color = PinkPrimary
                )
                Text(
                  text = "Direct Proctor Control Desk",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = uiState.proctorPhone,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${uiState.proctorPhone}"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("btn_call_proctor")
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = "Call Proctor", modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Call")
            }
          }
        }
      }

      // CAMPUS CORRIDORS & HOTSPOT SECTION
      item {
        Text(
          text = "CAMPUS SAFE CORRIDORS & HOTSPOTS",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.hotspots) { spot ->
        CampusSpotCard(
          spot = spot,
          onRequestEscort = { viewModel.requestCampusEscort(spot.spotName) }
        )
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}

@Composable
private fun CampusSpotCard(
  spot: CampusHotspot,
  onRequestEscort: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("spot_card_${spot.id}"),
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
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = spot.spotName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (spot.safetyRating.contains("Isolated")) PinkPrimaryDark.copy(alpha = 0.12f) else PinkContainer
        ) {
          Text(
            text = if (spot.safetyRating.contains("Isolated")) "⚠️ CAUTION" else "SAFE CORRIDOR",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (spot.safetyRating.contains("Isolated")) PinkPrimaryDark else PinkPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Text(
        text = "Security: ${spot.safetyRating}",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "Nearest Guard: ${spot.nearestGuardPost}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Text(
        text = "Tip: ${spot.tips}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onRequestEscort,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("btn_escort_${spot.id}")
        ) {
          Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Request Campus Escort", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
      }
    }
  }
}
