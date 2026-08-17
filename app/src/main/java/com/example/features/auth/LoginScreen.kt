package com.example.features.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafetyRedPrimary

@Composable
fun LoginScreen(
  viewModel: AuthViewModel,
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Logo Icon
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "RAMISA Logo",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(40.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "RAMISA",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        ),
        color = MaterialTheme.colorScheme.primary
      )

      Text(
        text = "Personal Safety System • Bangladesh",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Login Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Text(
            text = "Sign In",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Enter your phone number to access protection",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Phone Number Input
          OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::onPhoneChanged,
            label = { Text("Phone Number (বাংলাদেশ)") },
            placeholder = { Text("+880 17XX-XXXXXX") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Phone, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_phone")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Password Input
          OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_password")
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

          // Login Button
          Button(
            onClick = { viewModel.login(onLoginSuccess) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_login"),
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
                text = "LOG IN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Switch to Register
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Don't have an account?",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(
          onClick = onNavigateToRegister,
          modifier = Modifier.testTag("btn_go_to_register")
        ) {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
