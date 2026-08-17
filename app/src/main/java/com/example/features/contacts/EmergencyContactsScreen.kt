package com.example.features.contacts

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyContact
import com.example.ui.theme.SafeGreenContainer
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
  viewModel: ContactsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  var newName by remember { mutableStateOf("") }
  var newPhone by remember { mutableStateOf("") }
  var newRelationship by remember { mutableStateOf("Mother") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Emergency Contacts",
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
    floatingActionButton = {
      if (uiState.contacts.size < uiState.maxContactsLimit) {
        FloatingActionButton(
          onClick = { viewModel.showAddDialog(true) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_add_contact")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
        }
      }
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        // Status & Limit Header
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "TRUSTED CIRCLE (${uiState.contacts.size}/${uiState.maxContactsLimit})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "These contacts receive instant SMS & live alerts when SOS triggers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      items(uiState.contacts, key = { it.id }) { contact ->
        EmergencyContactItem(
          contact = contact,
          onDelete = { viewModel.deleteContact(contact.id) },
          onToggleSms = { viewModel.toggleSms(contact.id) },
          onToggleCall = { viewModel.toggleCall(contact.id) }
        )
      }

      if (uiState.contacts.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Contacts Configured",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Tap the + button to add up to 7 trusted emergency contacts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }

  // Add Contact Dialog
  if (uiState.isAddDialogVisible) {
    AlertDialog(
      onDismissRequest = { viewModel.showAddDialog(false) },
      title = {
        Text(
          text = "Add Emergency Contact",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Contact Name (নাম)") },
            placeholder = { Text("e.g. Mother, Sister") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_input_name")
          )

          OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("Phone Number (+880 বাংলাদেশ)") },
            placeholder = { Text("+880 17XX-XXXXXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_input_phone")
          )

          Text(
            text = "RELATIONSHIP",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          val relationships = listOf("Mother", "Father", "Sister", "Brother", "Friend", "Guardian", "Other")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            relationships.take(3).forEach { rel ->
              FilterChip(
                selected = newRelationship == rel,
                onClick = { newRelationship = rel },
                label = { Text(rel, fontSize = 12.sp) }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank() && newPhone.isNotBlank()) {
              viewModel.addContact(newName, newPhone, newRelationship)
              newName = ""
              newPhone = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("dialog_btn_save")
        ) {
          Text("Save Contact")
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showAddDialog(false) }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun EmergencyContactItem(
  contact: EmergencyContact,
  onDelete: () -> Unit,
  onToggleSms: () -> Unit,
  onToggleCall: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("contact_item_${contact.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Priority Badge
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "#${contact.priority}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              if (contact.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Verified",
                  tint = SafeGreenPrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
            Text(
              text = "${contact.phone} • ${contact.relationship}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove Contact",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Notification Capabilities Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilterChip(
          selected = contact.smsEnabled,
          onClick = onToggleSms,
          label = { Text("SMS Alert", fontSize = 11.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Outlined.Sms, contentDescription = null, modifier = Modifier.size(12.dp))
          }
        )

        FilterChip(
          selected = contact.callEnabled,
          onClick = onToggleCall,
          label = { Text("Call Enabled", fontSize = 11.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(12.dp))
          }
        )
      }
    }
  }
}
