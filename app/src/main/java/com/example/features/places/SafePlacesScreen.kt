package com.example.features.places

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PlaceType
import com.example.domain.model.SafePlace
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePlacesScreen(
  viewModel: SafePlacesViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.dialIntentNumber) {
    uiState.dialIntentNumber?.let { phone ->
      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
      try {
        context.startActivity(intent)
      } catch (_: Exception) {}
      viewModel.clearDialIntent()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Safe Places Radar (নিরাপদ স্থান)",
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

      // Search Field
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = viewModel::onSearchQueryChanged,
        placeholder = { Text("Search Thana, Hospital, Area (ধানমন্ডি, মিরপুর...)") },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_search_places")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Filter Chips Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = uiState.selectedFilter == null,
          onClick = { viewModel.onFilterSelected(null) },
          label = { Text("All", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedFilter == PlaceType.POLICE_STATION,
          onClick = { viewModel.onFilterSelected(PlaceType.POLICE_STATION) },
          label = { Text("Police / থানা", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedFilter == PlaceType.HOSPITAL,
          onClick = { viewModel.onFilterSelected(PlaceType.HOSPITAL) },
          label = { Text("Hospitals", fontSize = 11.sp) }
        )
        FilterChip(
          selected = uiState.selectedFilter == PlaceType.WOMEN_SUPPORT_CENTER,
          onClick = { viewModel.onFilterSelected(PlaceType.WOMEN_SUPPORT_CENTER) },
          label = { Text("Women Crisis (OCC)", fontSize = 11.sp) }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Safe places list
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(uiState.places, key = { it.id }) { place ->
          SafePlaceCard(
            place = place,
            onCall = { viewModel.onCallPlace(place.phoneNumber) },
            onNavigate = {
              val gmmIntentUri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${Uri.encode(place.name)}")
              val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
              try {
                context.startActivity(mapIntent)
              } catch (_: Exception) {}
            }
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
private fun SafePlaceCard(
  place: SafePlace,
  onCall: () -> Unit,
  onNavigate: () -> Unit
) {
  val icon: ImageVector = when (place.type) {
    PlaceType.POLICE_STATION -> Icons.Default.Security
    PlaceType.HOSPITAL -> Icons.Default.LocalHospital
    PlaceType.WOMEN_SUPPORT_CENTER -> Icons.Default.SupportAgent
    PlaceType.SAFE_ZONE -> Icons.Default.Shield
  }

  val typeTag = when (place.type) {
    PlaceType.POLICE_STATION -> "POLICE THANA"
    PlaceType.HOSPITAL -> "24/7 HOSPITAL"
    PlaceType.WOMEN_SUPPORT_CENTER -> "CRISIS HELPDESK"
    PlaceType.SAFE_ZONE -> "SAFE HAVEN"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("safe_place_card_${place.id}"),
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
            text = typeTag,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = SafeGreenContainer
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = SafeGreenDark,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${place.distanceKm} km away",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
              color = SafeGreenDark
            )
          }
        }
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
            text = place.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = place.address,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onCall,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("btn_call_place_${place.id}")
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("CALL DESK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onNavigate,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("btn_nav_place_${place.id}")
        ) {
          Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("DIRECTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
