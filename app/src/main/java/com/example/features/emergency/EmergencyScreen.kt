package com.example.features.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.location.EmergencyLocationManager
import com.example.domain.model.EmergencyStatus

@Composable
fun EmergencyScreen(
  viewModel: EmergencyViewModel,
  onResolveEmergency: () -> Unit,
  modifier: Modifier = Modifier,
  onManageContacts: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsState()
  var confirmSend by remember { mutableStateOf(false) }
  val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
    viewModel.refreshPermissions()
  }
  val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    viewModel.refreshLocation()
  }
  fun refreshLocation() {
    if (EmergencyLocationManager.hasLocationPermission(context)) viewModel.refreshLocation()
    else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
  }
  val enabledContacts = state.emergencyContacts.filter { it.smsEnabled }

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(if (state.status == EmergencyStatus.IDLE) "Emergency SOS" else "Emergency SMS status",
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("emergency_title"))
      Text("Review your recipients, location, and call options.")
      if (state.emergencyEventId.isNotBlank()) Text("Alert active • ${state.elapsedSeconds}s")
      Card(Modifier.fillMaxWidth().testTag("sms_status_card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(state.smsStatusSummary, fontWeight = FontWeight.SemiBold)
          Text("Network send confirmations: ${state.contactsNotifiedCount} • Delivery confirmations: ${state.deliveredCount}")
          Text("Sending and delivery are different. A network send confirmation does not mean a guardian has read your message.",
            style = MaterialTheme.typography.bodySmall)
          state.lastReport?.recipientReports?.forEach { recipient ->
            Text("${recipient.contactName}: ${recipient.sentParts}/${recipient.totalParts} parts sent, ${recipient.deliveredParts} delivered" +
              if (recipient.outcomeUnknown) " • outcome unknown" else "")
            recipient.errors.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
          }
        }
      }
      Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(state.locationLabel)
          if (state.location.isAvailable) Text("Coordinates: ${state.location.latitude}, ${state.location.longitude}")
          OutlinedButton(onClick = ::refreshLocation, modifier = Modifier.testTag("btn_emergency_refresh_gps")) {
            Text("Refresh device location")
          }
        }
      }
      Text("SMS-enabled contacts: ${enabledContacts.size}")
      enabledContacts.forEach { Text("${it.name} • ${it.phone}") }
      if (enabledContacts.isEmpty()) Text("Add a contact and enable SMS in Emergency Contacts before sending.",
        color = MaterialTheme.colorScheme.error)
      if (onManageContacts != null && enabledContacts.isEmpty()) {
        OutlinedButton(onClick = onManageContacts, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
          Text("Manage contacts")
        }
      }
      Button(onClick = {
        val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        if (!hasSms) smsPermission.launch(Manifest.permission.SEND_SMS) else confirmSend = true
      }, enabled = enabledContacts.isNotEmpty() && !state.isDispatching && !state.isSmsFallbackQueued,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_resend_sim_sms")) {
        Text(if (!state.isSmsPermissionGranted) "Allow SMS permission" else if (state.emergencyEventId.isBlank())
          "Send emergency SMS" else "Send another SMS…")
      }
      Text("SMS requires cellular service and may incur carrier charges. If location cannot be obtained, the message says it is unavailable.",
        style = MaterialTheme.typography.bodySmall)
      Text("Emergency phone numbers", style = MaterialTheme.typography.titleMedium)
      Text("These buttons open your phone's dialer. You choose whether to place the call.", style = MaterialTheme.typography.bodySmall)
      OutlinedButton(onClick = { openDialer(context, "999") }, modifier = Modifier.fillMaxWidth().testTag("btn_call_999")) { Text("Open dialer: 999") }
      OutlinedButton(onClick = { openDialer(context, "109") }, modifier = Modifier.fillMaxWidth().testTag("btn_call_109")) { Text("Open dialer: 109") }
      OutlinedButton(onClick = { openDialer(context, "1098") }, modifier = Modifier.fillMaxWidth().testTag("btn_call_1098")) { Text("Open dialer: 1098") }
      state.primaryContact?.let { primary ->
        OutlinedButton(onClick = { openDialer(context, primary.phone) }, modifier = Modifier.fillMaxWidth().testTag("btn_call_primary_contact")) {
          Text("Open dialer: ${primary.name}")
        }
      }
      if (state.emergencyEventId.isNotBlank()) Button(onClick = { viewModel.showResolveDialog(true) },
        modifier = Modifier.fillMaxWidth().testTag("btn_resolve_sos")) { Text("I am safe • resolve emergency") }
    }
  }
  if (confirmSend) AlertDialog(onDismissRequest = { confirmSend = false },
    title = { Text("Send emergency SMS?") }, text = {
      Text("This sends an SOS${if (state.location.isAvailable) " with your device location" else " (location may be unavailable)"} to ${enabledContacts.joinToString { it.name }}." +
        if (state.emergencyEventId.isNotBlank()) " This is a new send; previous messages cannot be recalled and recipients may receive another SMS." else " No phone call is placed.")
    }, confirmButton = { Button(onClick = { confirmSend = false; viewModel.triggerSos(autoCall = false) },
      modifier = Modifier.testTag("confirm_send_sms")) { Text("Send SMS now") } },
    dismissButton = { TextButton(onClick = { confirmSend = false }) { Text("Cancel") } })
  if (state.isResolveDialogVisible) AlertDialog(onDismissRequest = { viewModel.showResolveDialog(false) },
    title = { Text("Resolve emergency") }, text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Enter the safety PIN you configured in your profile. This closes the alert; already submitted SMS cannot be recalled.")
        OutlinedTextField(state.enteredPin, viewModel::onPinEntered, label = { Text("Safety PIN") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = PasswordVisualTransformation(), singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_resolve_pin"))
        state.pinErrorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
      }
    }, confirmButton = { Button(onClick = { viewModel.resolveEmergency(onResolveEmergency) },
      modifier = Modifier.testTag("dialog_btn_confirm_resolve")) { Text("Confirm safe") } },
    dismissButton = { TextButton(onClick = { viewModel.showResolveDialog(false) }) { Text("Cancel") } })
}

private fun openDialer(context: Context, number: String) {
  if (number.isBlank()) return
  runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))) }
}
