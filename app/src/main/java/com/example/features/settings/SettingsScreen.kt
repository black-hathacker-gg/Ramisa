package com.example.features.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.core.utils.BangladeshHotlines
import com.example.data.repository.AppSessionManager
import com.example.ui.components.SafetyActionRow

@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onLogout: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToLockScreenSos: () -> Unit,
  onNavigateToMesh: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by AppSessionManager.currentUser.collectAsStateWithLifecycle()
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsStateWithLifecycle()
  val hasExistingPin = !currentUser?.emergencyPin.isNullOrBlank()
  var showPinDialog by remember { mutableStateOf(false) }

  LifecycleResumeEffect(Unit) {
    viewModel.refreshDeviceSettings()
    onPauseOrDispose { }
  }

  val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) viewModel.setLockScreenGuard(true)
    else Toast.makeText(context, "Allow notifications to show the lock screen shortcut.", Toast.LENGTH_LONG).show()
  }
  val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) viewModel.setDirectSms(true)
    else Toast.makeText(context, "Allow SMS to enable the direct SMS shortcut.", Toast.LENGTH_LONG).show()
  }

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        Text(
          text = if (isBangla) "সেটিংস" else "Settings",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.SemiBold
        )
        SettingsGroup(title = if (isBangla) "প্রোফাইল ও ভাষা" else "Profile & language") {
          SafetyActionRow(
            title = currentUser?.name?.takeIf { it.isNotBlank() } ?: if (isBangla) "নিরাপত্তা প্রোফাইল" else "Safety profile",
            subtitle = if (isBangla) "ব্যক্তিগত ও জরুরি তথ্য" else "Personal and emergency details",
            icon = Icons.Default.Person,
            onClick = onNavigateToProfile,
            modifier = Modifier.testTag("settings_profile_card")
          )
          HorizontalDivider()
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).testTag("settings_lang_card")
          ) {
            Text(
              text = if (isBangla) "অ্যাপের ভাষা" else "App language",
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.titleSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              FilterChip(
                selected = !isBangla,
                onClick = { AppSessionManager.setBanglaLanguage(false) },
                label = { Text("English") },
                modifier = Modifier.testTag("chip_lang_en")
              )
              FilterChip(
                selected = isBangla,
                onClick = { AppSessionManager.setBanglaLanguage(true) },
                label = { Text("বাংলা") },
                modifier = Modifier.testTag("chip_lang_bn")
              )
            }
          }
        }

        SettingsGroup(title = if (isBangla) "নিরাপত্তা ও গোপনীয়তা" else "Safety & privacy") {
          SafetyActionRow(
            title = if (hasExistingPin) "Change safety PIN" else "Set safety PIN",
            subtitle = "Used to resolve emergency alerts",
            icon = Icons.Default.Lock,
            onClick = { viewModel.clearPinError(); showPinDialog = true }
          )
          HorizontalDivider()
          SettingsSwitch(
            title = "Lock screen shortcut",
            description = "Show the SOS notification when your device is locked",
            checked = state.isLockScreenGuardEnabled,
            onCheckedChange = { enabled ->
              when {
                !enabled -> viewModel.setLockScreenGuard(false)
                NotificationManagerCompat.from(context).areNotificationsEnabled() -> viewModel.setLockScreenGuard(true)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                  notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> context.startActivity(
                  Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
              }
            },
            testTag = "switch_lockscreen_guard_settings"
          )
          HorizontalDivider()
          SettingsSwitch(
            title = "Direct SOS SMS shortcut",
            description = "Add a Send SOS SMS action to the lock screen notification",
            checked = state.isDirectSmsEnabled,
            enabled = state.isLockScreenGuardEnabled,
            onCheckedChange = { enabled ->
              if (enabled && ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                smsPermission.launch(Manifest.permission.SEND_SMS)
              } else {
                viewModel.setDirectSms(enabled)
              }
            },
            testTag = "switch_sms_fallback"
          )
          HorizontalDivider()
          SafetyActionRow(
            title = "Lock screen options",
            subtitle = "Review and test available shortcuts",
            icon = Icons.Default.Lock,
            onClick = onNavigateToLockScreenSos
          )
          HorizontalDivider()
          SafetyActionRow(
            title = "Offline mesh",
            subtitle = "Bluetooth relay settings",
            icon = Icons.Default.Hub,
            onClick = onNavigateToMesh
          )
          HorizontalDivider()
          SettingsSwitch(
            title = "Ghost mode",
            description = if (hasExistingPin) "Disguise the launcher icon. Enter your safety PIN to return to RAMISA."
              else "Set a safety PIN before enabling the Calculator disguise.",
            checked = state.isGhostModeEnabled,
            enabled = hasExistingPin || state.isGhostModeEnabled,
            onCheckedChange = viewModel::setGhostMode,
            testTag = "switch_ghost_mode_settings"
          )
        }

        SettingsGroup(title = if (isBangla) "বাংলাদেশের হটলাইন" else "Bangladesh hotlines") {
          val numbers = listOf(
            BangladeshHotlines.EMERGENCY to "Emergency services",
            BangladeshHotlines.VIOLENCE_SUPPORT to "Violence against women and children",
            BangladeshHotlines.CHILD_HELP to "Child helpline",
            BangladeshHotlines.LEGAL_AID to "National legal aid"
          )
          numbers.forEachIndexed { index, (number, title) ->
            if (index > 0) HorizontalDivider()
            SafetyActionRow(
              title = title,
              subtitle = number,
              icon = Icons.Default.Call,
              onClick = {
                if (!BangladeshHotlines.openDialer(context, number)) {
                  Toast.makeText(context, "$title $number", Toast.LENGTH_LONG).show()
                }
              }
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RAMISA ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
          )
          TextButton(onClick = onLogout, modifier = Modifier.testTag("btn_logout")) {
            Text(
              if (currentUser?.id?.startsWith("guest_") == true) "Switch account" else "Sign out",
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }
  }

  if (showPinDialog) {
    PinDialog(hasExistingPin, state, viewModel, onDismiss = { showPinDialog = false })
  }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Column { content() }
    }
  }
}

@Composable
private fun SettingsSwitch(
  title: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
  enabled: Boolean = true
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      modifier = Modifier.testTag(testTag).semantics { contentDescription = title }
    )
  }
}

@Composable
private fun PinDialog(
  hasExistingPin: Boolean,
  state: SettingsUiState,
  viewModel: SettingsViewModel,
  onDismiss: () -> Unit
) {
  var currentPin by remember { mutableStateOf("") }
  var newPin by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = { if (!state.isSavingPin) onDismiss() },
    title = { Text(if (hasExistingPin) "Change safety PIN" else "Set safety PIN") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasExistingPin) {
          PinField(currentPin, "Current PIN", enabled = !state.isSavingPin) {
            currentPin = it.filter(Char::isDigit).take(4)
            viewModel.clearPinError()
          }
        }
        PinField(
          newPin, "New four-digit PIN",
          modifier = Modifier.testTag("dialog_input_new_pin"),
          enabled = !state.isSavingPin
        ) {
          newPin = it.mapNotNull(Char::digitToIntOrNull).take(4).joinToString("")
          viewModel.clearPinError()
        }
        state.pinError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
      }
    },
    confirmButton = {
      Button(
        onClick = { viewModel.updatePin(currentPin, newPin, onDismiss) },
        enabled = !state.isSavingPin && newPin.length == 4 && (!hasExistingPin || currentPin.length == 4)
      ) { Text(if (state.isSavingPin) "Saving…" else "Save PIN") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !state.isSavingPin) { Text("Cancel") }
    }
  )
}

@Composable
private fun PinField(
  value: String,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onValueChange: (String) -> Unit
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    label = { Text(label) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    visualTransformation = PasswordVisualTransformation(),
    singleLine = true,
    enabled = enabled
  )
}
