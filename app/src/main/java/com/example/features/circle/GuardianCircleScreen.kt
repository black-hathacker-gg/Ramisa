package com.example.features.circle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CircleMemberStatus
import com.example.domain.model.GuardianCircleMember
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianCircleScreen(
  viewModel: GuardianCircleViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Guardian Circles (সুরক্ষা সার্কেল)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Live peer safety & check-in monitor",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
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
      FloatingActionButton(
        onClick = { viewModel.openAddMemberDialog() },
        containerColor = PinkPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_circle_member")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
      }
    },
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Feedback Snackbar Bar
      item {
        AnimatedVisibility(visible = uiState.statusMessage != null) {
          uiState.statusMessage?.let { msg ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              color = PinkContainer
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = msg,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                  color = PinkPrimaryDark
                )
                TextButton(onClick = { viewModel.clearStatusMessage() }) {
                  Text("Dismiss", color = PinkPrimary)
                }
              }
            }
          }
        }
      }

      // My Status Broadcast Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("my_status_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PinkContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PinkPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "MY LIVE SAFETY STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = PinkPrimary
                  )
                  Text(
                    text = uiState.myStatus.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = PinkContainer
              ) {
                Text(
                  text = "Battery: ${uiState.myBatteryLevel}%",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Text(
              text = "Tap to update status for all circle guardians:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = uiState.myStatus == CircleMemberStatus.SAFE,
                onClick = { viewModel.updateMyStatus(CircleMemberStatus.SAFE) },
                label = { Text("Safe") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PinkPrimary,
                  selectedLabelColor = Color.White
                )
              )
              FilterChip(
                selected = uiState.myStatus == CircleMemberStatus.IN_TRANSIT,
                onClick = { viewModel.updateMyStatus(CircleMemberStatus.IN_TRANSIT) },
                label = { Text("In Rickshaw/Bus") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PinkPrimary,
                  selectedLabelColor = Color.White
                )
              )
              FilterChip(
                selected = uiState.myStatus == CircleMemberStatus.CHECKIN_DUE,
                onClick = { viewModel.updateMyStatus(CircleMemberStatus.CHECKIN_DUE) },
                label = { Text("Need Escort") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PinkPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }

      // Night Walk Safety Check-In Timer Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("interval_timer_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isIntervalCheckinActive) PinkContainer else MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = PinkPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "NIGHT WALK CHECK-IN TIMER",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (uiState.isIntervalCheckinActive) "Auto-prompt every ${uiState.checkinIntervalMinutes} mins"
                    else "Periodic safety check for late travel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = uiState.isIntervalCheckinActive,
                onCheckedChange = { viewModel.toggleIntervalCheckin(it, uiState.checkinIntervalMinutes) },
                modifier = Modifier.testTag("switch_interval_checkin")
              )
            }

            AnimatedVisibility(visible = uiState.isIntervalCheckinActive) {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val mins = uiState.nextCheckinSecondsRemaining / 60
                val secs = uiState.nextCheckinSecondsRemaining % 60
                val formattedTime = String.format("%02d:%02d", mins, secs)

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Next Check-in in: $formattedTime",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = PinkPrimaryDark
                  )
                  Button(
                    onClick = { viewModel.confirmSafeNow() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    modifier = Modifier.testTag("btn_confirm_safe_now")
                  ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("I'M SAFE NOW", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      // Circle Guardians List Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CIRCLE MEMBERS (${uiState.members.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = uiState.circleName,
            style = MaterialTheme.typography.labelSmall,
            color = PinkPrimary
          )
        }
      }

      // Members List
      items(uiState.members) { member ->
        CircleMemberCard(
          member = member,
          onRequestCheckin = { viewModel.requestCheckin(member.id) }
        )
      }

      item {
        Spacer(modifier = Modifier.height(70.dp))
      }
    }
  }

  // Add Member Dialog
  if (uiState.isAddMemberDialogOpen) {
    AddCircleMemberDialog(
      onDismiss = { viewModel.closeAddMemberDialog() },
      onAdd = { name, relation, phone ->
        viewModel.addMember(name, relation, phone)
      }
    )
  }
}

@Composable
private fun CircleMemberCard(
  member: GuardianCircleMember,
  onRequestCheckin: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("circle_member_${member.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(PinkContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = member.name.take(2).uppercase(),
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = PinkPrimaryDark
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = member.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${member.relation} • ${member.phone}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(member.status.badgeColorHex).copy(alpha = 0.12f)
        ) {
          Text(
            text = member.status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(member.status.badgeColorHex),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${member.lastLocation} (${member.lastUpdatedTime})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (member.batteryLevel <= 15) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
            contentDescription = null,
            tint = if (member.batteryLevel <= 15) PinkPrimaryDark else PinkPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = "${member.batteryLevel}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Check-in Request Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onRequestCheckin,
          shape = RoundedCornerShape(8.dp),
          enabled = !member.isCheckinRequested,
          modifier = Modifier.testTag("btn_ping_${member.id}")
        ) {
          Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (member.isCheckinRequested) "Check-in Requested" else "Request Check-In Ping",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}

@Composable
private fun AddCircleMemberDialog(
  onDismiss: () -> Unit,
  onAdd: (name: String, relation: String, phone: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var relation by remember { mutableStateOf("Friend") }
  var phone by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Add Guardian Circle Member",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("input_circle_member_name")
        )
        OutlinedTextField(
          value = relation,
          onValueChange = { relation = it },
          label = { Text("Relationship (e.g. Sister, Roommate)") },
          modifier = Modifier.fillMaxWidth().testTag("input_circle_member_relation")
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number (+880...)") },
          modifier = Modifier.fillMaxWidth().testTag("input_circle_member_phone")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            onAdd(name, relation, phone)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
        modifier = Modifier.testTag("btn_confirm_add_circle_member")
      ) {
        Text("Add to Circle")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
