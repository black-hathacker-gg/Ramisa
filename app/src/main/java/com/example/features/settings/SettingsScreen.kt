package com.example.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  onLogout: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "YOU",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PinkPrimary,
          titleContentColor = Color.White
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
    ) {
      // Profile Header Section
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(PinkPrimary)
          .padding(bottom = 32.dp, top = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(96.dp)
              .clip(CircleShape)
              .background(PinkContainer)
              .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "R",
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimaryDark
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Ramisa",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
          Text(
            text = "Safety ID: 8943-221",
            style = MaterialTheme.typography.labelMedium,
            color = PinkContainer
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Section: Personal
      SettingsSectionTitle(title = "PERSONAL")
      SettingsCard {
        SettingsItem(icon = Icons.Default.Person, title = "Account details", subtitle = "Name, Blood Group, Age", onClick = onNavigateToProfile)
        HorizontalDivider(color = PinkBorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(icon = Icons.Default.Contacts, title = "Safety Circle", subtitle = "Manage your trusted emergency contacts", onClick = { /* Navigate */ })
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Section: Security
      SettingsSectionTitle(title = "SECURITY & EMERGENCY")
      SettingsCard {
        SettingsItem(icon = Icons.Default.Lock, title = "Secret PIN", subtitle = "Update your emergency resolution PIN", onClick = { /* Show Dialog */ })
        HorizontalDivider(color = PinkBorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(icon = Icons.Default.SettingsCell, title = "Hardware Trigger", subtitle = "Power button & volume key gestures", onClick = { /* Navigate */ })
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Section: Preferences
      SettingsSectionTitle(title = "PREFERENCES")
      SettingsCard {
        SettingsItem(icon = Icons.Default.Language, title = "Language", subtitle = "English (US)", onClick = { /* Language Switch */ })
        HorizontalDivider(color = PinkBorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", subtitle = "Push & SMS alerts", onClick = { /* Notifications */ })
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Logout Button
      Button(
        onClick = onLogout,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinkContainer, contentColor = PinkPrimaryDark)
      ) {
        Text("Log Out", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }

      Spacer(modifier = Modifier.height(64.dp)) // padding for bottom nav
    }
  }
}

@Composable
fun SettingsSectionTitle(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
    color = TextSecondaryLight,
    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
  )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, PinkBorderSubtle),
    shadowElevation = 2.dp
  ) {
    Column(content = content)
  }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(PinkContainer.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = PinkPrimary)
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = PinkPrimaryDark
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondaryLight
        )
      }
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = TextSecondaryLight,
      modifier = Modifier.size(16.dp)
    )
  }
}
