package com.example.features.places

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*

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
            text = "SAFE PLACES",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PinkPrimary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White
        )
      )
    },
    modifier = modifier.fillMaxSize(),
    containerColor = SurfaceLight
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Search Field
          OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search nearby...", color = TextSecondaryLight) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PinkPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PinkPrimary,
              unfocusedBorderColor = PinkBorderSubtle,
              cursorColor = PinkPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_search_places")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Categories Grid / Filter Chips Row
          androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              SafePlaceFilterChip(
                selected = uiState.selectedFilter == null,
                onClick = { viewModel.onFilterSelected(null) },
                label = "All"
              )
            }
            item {
              SafePlaceFilterChip(
                selected = uiState.selectedFilter == PlaceType.POLICE_STATION,
                onClick = { viewModel.onFilterSelected(PlaceType.POLICE_STATION) },
                label = "Police Station"
              )
            }
            item {
              SafePlaceFilterChip(
                selected = uiState.selectedFilter == PlaceType.HOSPITAL,
                onClick = { viewModel.onFilterSelected(PlaceType.HOSPITAL) },
                label = "Hospital"
              )
            }
            item {
              SafePlaceFilterChip(
                selected = uiState.selectedFilter == PlaceType.WOMEN_SUPPORT_CENTER,
                onClick = { viewModel.onFilterSelected(PlaceType.WOMEN_SUPPORT_CENTER) },
                label = "University Security"
              )
            }
            item {
              SafePlaceFilterChip(
                selected = uiState.selectedFilter == PlaceType.SAFE_ZONE,
                onClick = { viewModel.onFilterSelected(PlaceType.SAFE_ZONE) },
                label = "Ramisa Verified"
              )
            }
          }
        }
      }

      // List of Places
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(uiState.places, key = { it.id }) { place ->
          SafePlaceCard(
            place = place,
            onCallClick = { viewModel.onCallPlace(place.phoneNumber) }
          )
        }
        
        if (uiState.places.isEmpty()) {
          item {
            Column(
              modifier = Modifier.fillMaxWidth().padding(48.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.NotListedLocation, contentDescription = null, tint = PinkPrimary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
              Spacer(modifier = Modifier.height(16.dp))
              Text("No safe places found", style = MaterialTheme.typography.titleMedium, color = PinkPrimaryDark)
            }
          }
        }
      }
    }
  }
}

@Composable
fun SafePlaceFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
  FilterChip(
    selected = selected,
    onClick = onClick,
    label = { Text(label, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else PinkPrimaryDark) },
    colors = FilterChipDefaults.filterChipColors(
      containerColor = PinkContainer.copy(alpha = 0.5f),
      selectedContainerColor = PinkPrimary
    ),
    border = FilterChipDefaults.filterChipBorder(
      enabled = true,
      selected = selected,
      borderColor = PinkBorderSubtle,
      selectedBorderColor = PinkPrimary
    ),
    shape = RoundedCornerShape(16.dp)
  )
}

@Composable
fun SafePlaceCard(
  place: SafePlace,
  onCallClick: () -> Unit
) {
  val icon = when (place.type) {
    PlaceType.POLICE_STATION -> Icons.Default.LocalPolice
    PlaceType.HOSPITAL -> Icons.Default.LocalHospital
    PlaceType.WOMEN_SUPPORT_CENTER -> Icons.Default.SupportAgent
    PlaceType.SAFE_ZONE -> Icons.Default.VerifiedUser
  }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, PinkBorderSubtle),
    shadowElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PinkContainer.copy(alpha = 0.5f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = PinkPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = place.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PinkPrimaryDark
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${place.distanceKm} km • ${place.area}",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondaryLight
          )
        }
        if (place.isOpen24x7) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = PinkContainer
          ) {
            Text(
              text = "24/7",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      
      Text(
        text = place.address,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = { /* Open maps intent */ },
          modifier = Modifier.weight(1f).height(44.dp),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, PinkPrimary),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary)
        ) {
          Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("NAVIGATE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Button(
          onClick = onCallClick,
          modifier = Modifier.weight(1f).height(44.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("CALL NOW", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}
