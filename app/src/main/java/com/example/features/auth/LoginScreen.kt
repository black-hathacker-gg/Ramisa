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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun LoginScreen(
  viewModel: AuthViewModel,
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onContinueAsGuest: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Surface(
    modifier = modifier.fillMaxSize(),
    color = SurfaceLight
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Hero Quote Highlight
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
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

      Spacer(modifier = Modifier.height(32.dp))

      // Logo Icon
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(Color.White)
          .border(2.dp, PinkContainer, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_app_logo),
          contentDescription = "RAMISA Logo",
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "RAMISA",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        ),
        color = PinkPrimaryDark
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Login Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
        ) {
          Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = PinkPrimaryDark
          )
          Text(
            text = "Log in to access your safety circle",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
          )
          
          Spacer(modifier = Modifier.height(24.dp))

          // Email Address Input
          OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email Address") },
            placeholder = { Text("your.email@example.com") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PinkPrimary)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PinkPrimary,
              focusedLabelColor = PinkPrimary,
              cursorColor = PinkPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_email")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Password Input
          OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PinkPrimary)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PinkPrimary,
              focusedLabelColor = PinkPrimary,
              cursorColor = PinkPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_password")
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

          // Login Button
          Button(
            onClick = { viewModel.login(onLoginSuccess) },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("btn_login"),
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
                text = "LOG IN",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Switch to Register
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Don't have an account?",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryLight
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(
          onClick = onNavigateToRegister,
          modifier = Modifier.testTag("btn_go_to_register")
        ) {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = PinkPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
