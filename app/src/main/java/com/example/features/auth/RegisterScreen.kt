package com.example.features.auth

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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
  viewModel: AuthViewModel,
  onRegisterSuccess: () -> Unit,
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
            text = "Create Protection Profile",
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
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Join RAMISA Safety Network",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Your safety profile is stored securely to assist responders and guardians.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Account Type / User Category
      Text(
        text = "ACCOUNT TYPE (ব্যবহারকারীর ধরন)",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        UserType.entries.forEach { type ->
          FilterChip(
            selected = uiState.userType == type,
            onClick = { viewModel.onUserTypeSelected(type) },
            label = {
              Text(
                when (type) {
                  UserType.ADULT -> "Adult (নারী)"
                  UserType.CHILD -> "Child (শিশু)"
                  UserType.GUARDIAN -> "Guardian (অভিভাবক)"
                }
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Age Category
      Text(
        text = "AGE GROUP (বয়সের গ্রুপ)",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          AgeCategory.CHILD_UNDER_12 to "<12",
          AgeCategory.TEEN_13_17 to "13-17",
          AgeCategory.ADULT_18_PLUS to "18+",
          AgeCategory.SENIOR to "60+"
        ).forEach { (cat, label) ->
          FilterChip(
            selected = uiState.ageCategory == cat,
            onClick = { viewModel.onAgeCategorySelected(cat) },
            label = { Text(label) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Full Name
      OutlinedTextField(
        value = uiState.name,
        onValueChange = viewModel::onNameChanged,
        label = { Text("Full Name (পুরো নাম)") },
        placeholder = { Text("e.g. Sadia Rahman") },
        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_name")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Phone Number
      OutlinedTextField(
        value = uiState.phone,
        onValueChange = viewModel::onPhoneChanged,
        label = { Text("Phone Number (+880 বাংলাদেশ)") },
        placeholder = { Text("+880 1712-345678") },
        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_phone")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Blood Group & Medical Note
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = uiState.bloodGroup,
          onValueChange = viewModel::onBloodGroupChanged,
          label = { Text("Blood Group") },
          placeholder = { Text("B+") },
          leadingIcon = { Icon(imageVector = Icons.Default.Bloodtype, contentDescription = null) },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("input_reg_blood")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = uiState.emergencyNote,
        onValueChange = viewModel::onEmergencyNoteChanged,
        label = { Text("Emergency & Medical Notes (ঐচ্ছিক)") },
        placeholder = { Text("e.g. Asthmatic, carries emergency medicine") },
        leadingIcon = { Icon(imageVector = Icons.Default.Notes, contentDescription = null) },
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_notes")
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4-Digit Emergency PIN
      OutlinedTextField(
        value = uiState.emergencyPin,
        onValueChange = { if (it.length <= 4) viewModel.onEmergencyPinChanged(it) },
        label = { Text("4-Digit Emergency Cancellation PIN") },
        placeholder = { Text("e.g. 1234") },
        supportingText = { Text("Required to verify false alarms or resolve emergency SOS") },
        leadingIcon = { Icon(imageVector = Icons.Default.Pin, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_pin")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Password
      OutlinedTextField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChanged,
        label = { Text("Account Password") },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_reg_password")
      )

      if (uiState.errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = uiState.errorMessage ?: "",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = { viewModel.register(onRegisterSuccess) },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_register_submit"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = !uiState.isLoading
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.5.dp
          )
        } else {
          Text(
            text = "CREATE SAFETY ACCOUNT",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
