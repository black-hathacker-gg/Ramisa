package com.example.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: ProfileViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "ACCOUNT DETAILS",
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
          navigationIconContentColor = Color.White
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
        .verticalScroll(scrollState)
        .padding(24.dp)
    ) {
      if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = PinkPrimary)
        }
      } else {
        // Editable fields
        OutlinedTextField(
          value = uiState.editName,
          onValueChange = { viewModel.onEditNameChanged(it) },
          label = { Text("Full Name") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PinkPrimary) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = uiState.editPhone,
          onValueChange = { viewModel.onEditPhoneChanged(it) },
          label = { Text("Phone Number") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PinkPrimary) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Blood Group
        Text("Blood Group", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextSecondaryLight)
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val groups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
          items(groups.size) { index ->
            val bg = groups[index]
            FilterChip(
              selected = uiState.editBloodGroup == bg,
              onClick = { viewModel.onEditBloodGroupChanged(bg) },
              label = { Text(bg, fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PinkPrimary,
                selectedLabelColor = Color.White
              ),
              border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.editBloodGroup == bg, borderColor = PinkBorderSubtle, selectedBorderColor = PinkPrimary)
            )
          }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
          onClick = { viewModel.saveProfile() },
          modifier = Modifier.fillMaxWidth().height(56.dp),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
        ) {
          Icon(Icons.Default.Save, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("SAVE CHANGES", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
        }
        
        if (uiState.message != null) {
          Spacer(modifier = Modifier.height(16.dp))
          Surface(shape = RoundedCornerShape(8.dp), color = PinkContainer) {
            Text(uiState.message!!, modifier = Modifier.padding(16.dp), color = PinkPrimaryDark, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }
}
