package com.example.features.legalaid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Share
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
fun LegalAidScreen(
  viewModel: LegalAidViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  val categories = listOf(
    "Street Harassment / ইভটিজিং",
    "Cyber Blackmail / সাইবার হেনস্তা",
    "Stalking / পিছু নেওয়া ও হুমকি",
    "Domestic Abuse / পারিবারিক নির্যাতন"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "ডিজিটাল জিডি ও আইনি সহায়তা",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Bangladesh Police GD & Cyber Harassment Drafter",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
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

      // Feedback banner
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
                  color = PinkPrimaryDark,
                  modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.clearFeedback() }) {
                  Text("Dismiss", color = PinkPrimary)
                }
              }
            }
          }
        }
      }

      // GOV TOLL-FREE LEGAL AID BANNER
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_gov_legal_aid_banner"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(PinkPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color.White)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "জাতীয় জরুরি আইনি সহায়তা হটলাইন",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark
                )
                Text(
                  text = "ডায়াল করুন: ১৬৪৩০ (টোল ফ্রি)",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "বিনামূল্যে আইনি পরামর্শ ও ৬৪ জেলায় সরকারি উকিল",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:16430"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("btn_dial_16430")
            ) {
              Text("16430", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // THANA GENERAL DIARY (GD) DRAFTER
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = PinkPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "থানা জিডি / অনলাইন অভিযোগ ড্রাফটার",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Text(
              text = "অভিযোগের ধরন নির্বাচন করুন:",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(categories) { cat ->
                val isSelected = uiState.generalDiaryDraftCategory == cat
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = if (isSelected) PinkPrimary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.clickable { viewModel.updateGdCategory(cat) }
                ) {
                  Text(
                    text = cat,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }

            OutlinedTextField(
              value = uiState.applicantName,
              onValueChange = { viewModel.updateApplicantName(it) },
              label = { Text("আপনার নাম (Applicant Name)") },
              placeholder = { Text("যেমন: সুমাইয়া আক্তার") },
              modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = uiState.applicantPhone,
                onValueChange = { viewModel.updateApplicantPhone(it) },
                label = { Text("মোবাইল নম্বর") },
                placeholder = { Text("017XXXXXXXX") },
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = uiState.applicantNid,
                onValueChange = { viewModel.updateApplicantNid(it) },
                label = { Text("NID / জন্মসনদ") },
                placeholder = { Text("ঐচ্ছিক") },
                modifier = Modifier.weight(1f)
              )
            }

            OutlinedTextField(
              value = uiState.incidentThana,
              onValueChange = { viewModel.updateGdThana(it) },
              label = { Text("সংশ্লিষ্ট থানা (Target Thana)") },
              modifier = Modifier.fillMaxWidth().testTag("input_gd_thana")
            )

            OutlinedTextField(
              value = uiState.incidentLocationDetail,
              onValueChange = { viewModel.updateLocationDetail(it) },
              label = { Text("ঘটনাস্থল (Incident Location)") },
              placeholder = { Text("যেমন: কার্জন হল গেট, ঢাকা বিশ্ববিদ্যালয়") },
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = uiState.incidentDescription,
              onValueChange = { viewModel.updateIncidentDescription(it) },
              label = { Text("ঘটনার সংক্ষিপ্ত বিবরণ (Description)") },
              placeholder = { Text("কী ঘটেছিল তা সংক্ষেপে লিখুন...") },
              minLines = 2,
              modifier = Modifier.fillMaxWidth()
            )

            Button(
              onClick = { viewModel.generateGeneralDiaryDraft() },
              colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("btn_generate_gd_draft")
            ) {
              Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("জিডি ফরম্যাট প্রস্তুত করুন (Generate GD Draft)", fontWeight = FontWeight.Bold)
            }

            uiState.gdDraftText?.let { draft ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().testTag("surface_gd_draft_result")
              ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(
                    text = draft,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedButton(
                      onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("RAMISA GD Draft", draft)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "জিডি টেক্সট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("কপি করুন", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                      onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                          type = "text/plain"
                          putExtra(Intent.EXTRA_SUBJECT, "জিডি আবেদনপত্র - ${uiState.generalDiaryDraftCategory}")
                          putExtra(Intent.EXTRA_TEXT, draft)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "জিডি আবেদন শেয়ার করুন"))
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("শেয়ার করুন", style = MaterialTheme.typography.labelSmall)
                    }
                  }
                }
              }
            }
          }
        }
      }

      // POLICE STATIONS & CYBER HELPLINES DIRECTORY
      item {
        Text(
          text = "জরুরি থানা ও সাইবার পুলিশ কন্ট্রোল রুম",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.policeHotlines) { police ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LocalPolice,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = police.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${police.thana} • ${police.phone}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${police.phone}"))
                context.startActivity(intent)
              },
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("কল", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }

      // LEGAL AID FOUNDATIONS IN BANGLADESH
      item {
        Text(
          text = "বিনামূল্যে আইনি সহায়তা সংস্থা (LEGAL AID)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      items(uiState.legalOrgs) { org ->
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
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = org.nameBengali,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = org.name,
                  style = MaterialTheme.typography.bodySmall,
                  color = PinkPrimary
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = PinkContainer
              ) {
                Text(
                  text = org.serviceType,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = PinkPrimaryDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Text(
              text = "ঠিকানা: ${org.address}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            org.rightsSummary.forEach { right ->
              Text(
                text = right,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${org.hotline}"))
                  context.startActivity(intent)
                },
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("কল ${org.hotline}", style = MaterialTheme.typography.labelSmall)
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
