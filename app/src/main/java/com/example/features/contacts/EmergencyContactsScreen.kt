package com.example.features.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyContact
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
  viewModel: ContactsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "SAFETY CIRCLE",
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
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = PinkPrimary,
        contentColor = Color.White,
        shape = CircleShape
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Contact")
      }
    },
    modifier = modifier.fillMaxSize(),
    containerColor = SurfaceLight
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (uiState.contacts.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.GroupAdd,
              contentDescription = null,
              modifier = Modifier.size(80.dp),
              tint = PinkBorderSubtle
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Your Safety Circle is empty.",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Add trusted contacts to notify them during an emergency.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondaryLight
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          item {
            Text(
              text = "TRUSTED CONTACTS",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
              color = TextSecondaryLight,
              modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
          }
          items(uiState.contacts, key = { it.id }) { contact ->
            ContactCard(
              contact = contact,
              onToggleSms = { viewModel.toggleSms(contact.id) },
              onToggleCall = { viewModel.toggleCall(contact.id) },
              onDelete = { viewModel.deleteContact(contact.id) }
            )
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddContactDialog(
      onDismiss = { showAddDialog = false },
      onAdd = { name, phone, rel ->
        viewModel.addContact(name, phone, rel)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun ContactCard(
  contact: EmergencyContact,
  onToggleSms: (Boolean) -> Unit,
  onToggleCall: (Boolean) -> Unit,
  onDelete: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, PinkBorderSubtle),
    shadowElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PinkContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = contact.name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PinkPrimary
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = contact.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PinkPrimaryDark
          )
          Text(
            text = "${contact.relationship} • ${contact.phone}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
          )
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SafetyRedPrimary)
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = PinkBorderSubtle)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        AlertToggle(
          icon = Icons.Outlined.Sms,
          label = "SMS Alert",
          isChecked = contact.smsEnabled,
          onCheckedChange = onToggleSms
        )
        AlertToggle(
          icon = Icons.Outlined.Call,
          label = "Auto Call",
          isChecked = contact.callEnabled,
          onCheckedChange = onToggleCall
        )
      }
    }
  }
}

@Composable
fun AlertToggle(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckedChange(!isChecked) }) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (isChecked) PinkPrimary else TextSecondaryLight,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
      color = if (isChecked) PinkPrimaryDark else TextSecondaryLight
    )
    Spacer(modifier = Modifier.width(8.dp))
    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = PinkPrimary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = PinkBorderSubtle,
        uncheckedBorderColor = Color.Transparent
      )
    )
  }
}

@Composable
fun AddContactDialog(
  onDismiss: () -> Unit,
  onAdd: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var relationship by remember { mutableStateOf("Family") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceCardLight,
    title = {
      Text(
        text = "Add Trusted Contact",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = PinkPrimaryDark
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
        )
        OutlinedTextField(
          value = relationship,
          onValueChange = { relationship = it },
          label = { Text("Relationship (e.g., Mother)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(name, phone, relationship) },
        enabled = name.isNotBlank() && phone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary, contentColor = Color.White)
      ) {
        Text("ADD")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = PinkPrimary)) {
        Text("CANCEL")
      }
    }
  )
}
