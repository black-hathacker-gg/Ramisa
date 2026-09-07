package com.example.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgeCategory
import com.example.domain.model.UserType
import com.example.ui.theme.*

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
            text = "CREATE ACCOUNT",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
        .fillMaxWidth()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
      
      // Hero Quote Highlight
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = PinkPrimary
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Please don’t stop flying,\nfly freely to your desired sky",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              fontStyle = FontStyle.Italic,
              letterSpacing = 0.5.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
          )
        }
      }

      Text(
        text = "Join RAMISA Safety Network",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
        color = PinkPrimaryDark
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Your safety profile is stored securely to assist responders and guardians.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Account Type / User Category
      Text(
        text = "ACCOUNT TYPE",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
        color = TextSecondaryLight
      )
      Spacer(modifier = Modifier.height(8.dp))
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
                text = when (type) {
                  UserType.ADULT -> "Adult"
                  UserType.CHILD -> "Child"
                  UserType.GUARDIAN -> "Guardian"
                },
                fontWeight = FontWeight.Bold
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PinkPrimary,
              selectedLabelColor = Color.White,
              containerColor = PinkContainer.copy(alpha = 0.4f),
              labelColor = PinkPrimaryDark
            ),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.userType == type, borderColor = PinkBorderSubtle, selectedBorderColor = PinkPrimary)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Full Name
      OutlinedTextField(
        value = uiState.name,
        onValueChange = viewModel::onNameChanged,
        label = { Text("Full Name") },
        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PinkPrimary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_name"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary, cursorColor = PinkPrimary)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Email Input
      OutlinedTextField(
        value = uiState.email,
        onValueChange = viewModel::onEmailChanged,
        label = { Text("Email Address") },
        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PinkPrimary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_email"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary, cursorColor = PinkPrimary)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Phone Number
      OutlinedTextField(
        value = uiState.phone,
        onValueChange = viewModel::onPhoneChanged,
        label = { Text("Phone Number") },
        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PinkPrimary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_phone"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary, cursorColor = PinkPrimary)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Password
      OutlinedTextField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChanged,
        label = { Text("Account Password") },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PinkPrimary) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_password"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary, cursorColor = PinkPrimary)
      )

      if (uiState.errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = uiState.errorMessage ?: "",
          style = MaterialTheme.typography.bodySmall,
          color = SafetyRedPrimary
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Button(
        onClick = { viewModel.register(onRegisterSuccess) },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("btn_register_submit"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White),
        enabled = !uiState.isLoading
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp
          )
        } else {
          Text(
            text = "CREATE ACCOUNT",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
