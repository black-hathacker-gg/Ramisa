package com.example.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedLight
import com.example.ui.theme.SafetyRedPrimary

@Composable
fun WelcomeScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToRegister: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // App Logo Badge
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(SafetyRedPrimary.copy(alpha = 0.12f))
          .border(2.dp, SafetyRedPrimary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "RAMISA Safety Shield",
          tint = SafetyRedPrimary,
          modifier = Modifier.size(54.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "RAMISA",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Personal Safety & Rapid Response for Women & Children",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Feature Highlights Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          FeatureHighlightRow(
            icon = Icons.Outlined.CheckCircle,
            title = "1-Tap Urgent SOS Alerting",
            subtitle = "Direct dispatch to trusted contacts & national services"
          )
          FeatureHighlightRow(
            icon = Icons.Outlined.Security,
            title = "Bangladesh 999 & 109 Hotlines",
            subtitle = "Instant, toll-free national emergency connectivity"
          )
          FeatureHighlightRow(
            icon = Icons.Default.Lock,
            title = "Privacy-First Local Vault",
            subtitle = "Zero tracking; emergency PIN resolution protection"
          )
        }
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Action Buttons
      Button(
        onClick = onNavigateToRegister,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_welcome_register"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(
          text = "CREATE SAFETY ACCOUNT",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onNavigateToLogin,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_welcome_login"),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "LOG IN (সাইন ইন)",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun FeatureHighlightRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
