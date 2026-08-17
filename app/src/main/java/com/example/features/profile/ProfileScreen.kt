package com.example.features.profile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserType
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
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

  val createdDateFormatted = remember(uiState.user.createdAt) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    sdf.format(Date(uiState.user.createdAt))
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (uiState.isEditing) "Edit Safety Profile" else "Safety Profile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = {
            if (uiState.isEditing) viewModel.setEditing(false) else onNavigateBack()
          }) {
            Icon(
              imageVector = if (uiState.isEditing) Icons.Default.Close else Icons.Default.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          if (!uiState.isEditing) {
            IconButton(
              onClick = { viewModel.setEditing(true) },
              modifier = Modifier.testTag("btn_edit_profile")
            ) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
            }
          } else {
            IconButton(
              onClick = { viewModel.saveProfile() },
              modifier = Modifier.testTag("btn_save_profile")
            ) {
              Icon(imageVector = Icons.Default.Save, contentDescription = "Save Profile", tint = SafeGreenPrimary)
            }
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
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (uiState.message != null) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SafeGreenContainer)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreenDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = uiState.message ?: "", color = SafeGreenDark, style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      if (uiState.errorMessage != null) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = uiState.errorMessage ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      if (!uiState.isEditing) {
        // VIEW MODE
        // 1. Header Profile Card
        Card(
          modifier = Modifier.fillMaxWidth().testTag("profile_view_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              val initials = uiState.user.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
              Text(
                text = initials.ifBlank { "R" },
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = uiState.user.name,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = uiState.user.phone,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Text(
                  text = when (uiState.user.userType) {
                    UserType.ADULT -> "Adult (নারী)"
                    UserType.CHILD -> "Child (শিশু)"
                    UserType.GUARDIAN -> "Guardian (অভিভাবক)"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SafeGreenContainer
              ) {
                Text(
                  text = "Blood: ${uiState.user.bloodGroup}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = SafeGreenDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }

        // 2. Emergency Safety Vault
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "SAFETY VAULT DETAILS",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = MaterialTheme.colorScheme.primary
            )

            ProfileDetailRow(
              icon = Icons.Default.Lock,
              label = "Emergency Resolution PIN",
              value = "•••• (4-digit security PIN configured)"
            )

            ProfileDetailRow(
              icon = Icons.Default.Bloodtype,
              label = "Blood Group",
              value = uiState.user.bloodGroup
            )

            ProfileDetailRow(
              icon = Icons.Default.Notes,
              label = "Responder & Medical Notes",
              value = uiState.user.emergencyNote.ifBlank { "No medical or special notes provided" }
            )

            if (uiState.user.guardianName != null || uiState.user.guardianPhone != null) {
              ProfileDetailRow(
                icon = Icons.Default.Shield,
                label = "Designated Guardian Link",
                value = "${uiState.user.guardianName ?: "Guardian"} (${uiState.user.guardianPhone ?: "No phone"})"
              )
            }
          }
        }

        // 3. Account Meta Info
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "ACCOUNT METRICS",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "User ID: ${uiState.user.id}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Protection Active Since: $createdDateFormatted",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      } else {
        // EDIT MODE
        Card(
          modifier = Modifier.fillMaxWidth().testTag("profile_edit_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "Edit Information",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
              value = uiState.editName,
              onValueChange = viewModel::onEditNameChanged,
              label = { Text("Full Name (পুরো নাম)") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_name")
            )

            OutlinedTextField(
              value = uiState.editPhone,
              onValueChange = viewModel::onEditPhoneChanged,
              label = { Text("Phone Number (+880 বাংলাদেশ)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_phone")
            )

            OutlinedTextField(
              value = uiState.editBloodGroup,
              onValueChange = viewModel::onEditBloodGroupChanged,
              label = { Text("Blood Group") },
              leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_blood")
            )

            OutlinedTextField(
              value = uiState.editEmergencyNote,
              onValueChange = viewModel::onEditEmergencyNoteChanged,
              label = { Text("Emergency Notes & Allergies") },
              leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
              maxLines = 3,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_notes")
            )

            Text(
              text = "DESIGNATED GUARDIAN (ঐচ্ছিক)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
              value = uiState.editGuardianName,
              onValueChange = viewModel::onEditGuardianNameChanged,
              label = { Text("Guardian Name") },
              placeholder = { Text("e.g. Father, Mother") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_guardian_name")
            )

            OutlinedTextField(
              value = uiState.editGuardianPhone,
              onValueChange = viewModel::onEditGuardianPhoneChanged,
              label = { Text("Guardian Phone") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("input_edit_guardian_phone")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = { viewModel.saveProfile() },
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_edit_profile"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              enabled = !uiState.isLoading
            ) {
              if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
              } else {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun ProfileDetailRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
