package com.example.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onContinueAsGuest: () -> Unit = {},
  modifier: Modifier = Modifier
) {
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

      // Hero Quote (Very Highlighted as per user request)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = PinkPrimary,
        shadowElevation = 8.dp
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "“",
            style = MaterialTheme.typography.displayLarge.copy(
              fontWeight = FontWeight.Black,
              color = PinkContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.offset(y = 12.dp)
          )
          Text(
            text = "Please don’t stop flying,\nfly freely to your desired sky",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontStyle = FontStyle.Italic,
              letterSpacing = 0.5.sp,
              lineHeight = 28.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.White))
        }
      }

      Spacer(modifier = Modifier.height(48.dp))

      // App Logo Badge
      Box(
        modifier = Modifier
          .size(120.dp)
          .clip(CircleShape)
          .background(Color.White)
          .border(4.dp, PinkContainer, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_app_logo),
          contentDescription = "RAMISA Safety Logo",
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "RAMISA",
        style = MaterialTheme.typography.displaySmall.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 4.sp
        ),
        color = PinkPrimaryDark
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Personal Safety & Rapid Response\nfor Women & Children",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = TextSecondaryLight,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Action Buttons
      Button(
        onClick = onNavigateToRegister,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("btn_welcome_register"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
      ) {
        Text(
          text = "CREATE SAFETY ACCOUNT",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = onNavigateToLogin,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("btn_welcome_login"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, PinkPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary)
      ) {
        Text(
          text = "LOG IN",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Guest Login Button
      TextButton(
        onClick = onContinueAsGuest,
        modifier = Modifier.testTag("btn_welcome_guest"),
      ) {
        Text(
          text = "Continue as Guest",
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
