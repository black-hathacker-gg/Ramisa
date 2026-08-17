package com.example.features.volunteer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerEscortScreen(
  viewModel: VolunteerEscortViewModel,
  onNavigateBack: () -> Unit,
  onTriggerSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Peer Escort & Volunteer Network",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Verified student volunteers & safe night escorts (ভলান্টিয়ার এসকোর্ট)",
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

      // Notification banner
      item {
        AnimatedVisibility(visible = uiState.feedbackMessage != null) {
          uiState.feedbackMessage?.let { msg ->
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              color = PinkContainer
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = msg,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark
                )
                TextButton(onClick = { viewModel.clearFeedback() }) {
                  Text("Dismiss", color = PinkPrimary)
                }
              }
            }
          }
        }
      }

      // ACTIVE MATCHED ESCORT CARD OR REQUEST FORM
      if (uiState.isEscortRequested && uiState.matchedVolunteer != null) {
        val vol = uiState.matchedVolunteer!!
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_active_escort_match"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PinkContainer),
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
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(PinkPrimary),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "ESCORT EN ROUTE",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = PinkPrimaryDark
                    )
                    Text(
                      text = vol.name,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = PinkPrimary
                ) {
                  Text(
                    text = "${vol.distanceMeters}m away",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Text(
                text = "Affiliation: ${vol.universityOrOrg}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )

              Text(
                text = "Meeting at: ${uiState.pickupLocation} ➔ ${uiState.destination}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = PinkPrimaryDark
              )

              HorizontalDivider(color = PinkPrimary.copy(alpha = 0.2f))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vol.phone}"))
                    context.startActivity(intent)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("btn_call_escort")
                ) {
                  Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Call Escort")
                }

                OutlinedButton(
                  onClick = { viewModel.cancelEscort() },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).testTag("btn_finish_escort")
                ) {
                  Text("Arrived Safely")
                }
              }
            }
          }
        }
      } else {
        // REQUEST ESCORT FORM
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_request_escort_form"),
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
              Text(
                text = "REQUEST PEER SAFETY ESCORT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = PinkPrimary
              )

              OutlinedTextField(
                value = uiState.pickupLocation,
                onValueChange = { viewModel.updatePickup(it) },
                label = { Text("Pickup Point (পিকআপ পয়েন্ট)") },
                modifier = Modifier.fillMaxWidth().testTag("input_escort_pickup")
              )

              OutlinedTextField(
                value = uiState.destination,
                onValueChange = { viewModel.updateDestination(it) },
                label = { Text("Destination / Hall (গন্তব্য)") },
                modifier = Modifier.fillMaxWidth().testTag("input_escort_destination")
              )

              Button(
                onClick = { viewModel.requestEscort() },
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("btn_broadcast_escort_request")
              ) {
                Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISPATCH ESCORT REQUEST", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // NEARBY VOLUNTEERS
      item {
        Text(
          text = "VERIFIED NEARBY VOLUNTEERS (যাচাইকৃত ভলান্টিয়ার)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.activeVolunteers) { vol ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(if (vol.isAvailableNow) PinkContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = if (vol.isAvailableNow) PinkPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = vol.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (vol.studentIdVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Verified ID",
                      tint = PinkPrimary,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }
                Text(
                  text = vol.universityOrOrg,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${vol.distanceMeters}m away • ${vol.ratingsCount} completed escorts",
                  style = MaterialTheme.typography.labelSmall,
                  color = PinkPrimary
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (vol.isAvailableNow) PinkContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = if (vol.isAvailableNow) "AVAILABLE" else "BUSY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (vol.isAvailableNow) PinkPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}
