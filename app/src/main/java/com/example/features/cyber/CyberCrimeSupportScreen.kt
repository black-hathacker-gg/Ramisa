package com.example.features.cyber

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
fun CyberCrimeSupportScreen(
  viewModel: CyberCrimeViewModel,
  onNavigateBack: () -> Unit,
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
              text = "Cyber Harassment & PCSW",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Police Cyber Support for Women (সাইবার সাপোর্ট)",
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

      // PCSW DIRECT HOTLINE CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_pcsw_direct"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer),
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
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "OFFICIAL BANGLADESH POLICE DESK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = PinkPrimaryDark
                  )
                  Text(
                    text = "Police Cyber Support for Women",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            Text(
              text = "Operated completely by female police officers. Confidential handling for hacking, fake profiles, blackmail, and unauthorized media leaks.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01320000888"))
                  context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_call_pcsw")
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call 01320000888", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:cybersupport.women@police.gov.bd")
                    putExtra(Intent.EXTRA_SUBJECT, "[Urgent] Cyber Harassment Complaint Report")
                  }
                  context.startActivity(intent)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_email_pcsw")
              ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Email PCSW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // EVIDENCE LOG & REPORT PACKET MAKER
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
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
              text = "PREPARE CYBER COMPLAINT PACKET",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = PinkPrimary
            )

            OutlinedTextField(
              value = uiState.reportedIssueType,
              onValueChange = { viewModel.updateReportedIssue(it) },
              label = { Text("Incident Category") },
              modifier = Modifier.fillMaxWidth().testTag("input_cyber_category")
            )

            OutlinedTextField(
              value = uiState.incidentNotes,
              onValueChange = { viewModel.updateIncidentNotes(it) },
              label = { Text("URL / Profile Links / Incident Summary") },
              placeholder = { Text("Paste offender profile links, chat dates, and threatening account handles...") },
              modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_cyber_notes")
            )

            Button(
              onClick = { viewModel.submitCyberComplaintDraft() },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("btn_generate_complaint")
            ) {
              Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Generate Timestamped Evidence Packet", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // CYBER RESOURCE UNITS
      item {
        Text(
          text = "INSTITUTIONAL CYBER CELLS IN BANGLADESH",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.resources) { res ->
        Card(
          modifier = Modifier.fillMaxWidth(),
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
              Column {
                Text(
                  text = res.organization,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = res.organizationBengali,
                  style = MaterialTheme.typography.bodySmall,
                  color = PinkPrimary
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = PinkContainer
              ) {
                Text(
                  text = res.category,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            res.guideSteps.forEach { step ->
              Text(
                text = step,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${res.hotline}"))
                  context.startActivity(intent)
                },
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hotline ${res.hotline}", style = MaterialTheme.typography.labelSmall)
              }
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
