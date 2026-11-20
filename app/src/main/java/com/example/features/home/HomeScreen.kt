package com.example.features.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.community.CommunityManager
import com.example.core.location.EmergencyLocationManager
import com.example.core.utils.BangladeshHotlines
import com.example.data.repository.AppSessionManager
import com.example.ui.components.SafetyActionRow
import com.example.ui.components.RamisaGuideSheet

@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  onNavigateToSafeJourney: () -> Unit,
  onNavigateToContacts: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onTriggerSos: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  onNavigateToFakeCall: () -> Unit = {},
  onNavigateToCommunitySafety: () -> Unit = {},
  onNavigateToHardwareTrigger: () -> Unit = {},
  onNavigateToSafetyTimer: () -> Unit = {},
  onNavigateToSafePlaces: () -> Unit = {},
  onNavigateToDangerMap: () -> Unit = {},
  onNavigateToTools: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsStateWithLifecycle()
  val activeAlerts by CommunityManager.activeSosAlerts.collectAsStateWithLifecycle()
  val latestAlert = activeAlerts.firstOrNull { it.status != "RESOLVED" }
  val context = LocalContext.current
  val preferences = remember(context) {
    context.applicationContext.getSharedPreferences("ramisa_guide", android.content.Context.MODE_PRIVATE)
  }
  var guideComplete by remember { mutableStateOf(preferences.getInt("completed_version", 0) >= 1) }
  var showGuide by rememberSaveable { mutableStateOf(false) }
  val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
    if (grants.values.any { it }) viewModel.refreshLocation(context)
  }

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier
          .widthIn(max = 640.dp)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        DashboardHeader(isBangla, onNavigateToProfile) { showGuide = true }
        ContactsReadiness(state, isBangla, onNavigateToContacts)
        EmergencyPanel(isBangla, onTriggerSos)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (isBangla) "দ্রুত ব্যবহার" else "Quick actions",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onNavigateToTools, modifier = Modifier.testTag("btn_all_tools")) {
              Text(if (isBangla) "সব টুল" else "All tools")
              Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp).size(16.dp)
              )
            }
          }
          Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            SafetyShortcut(
              title = if (isBangla) "নিরাপদ যাত্রা" else "Safe journey",
              icon = Icons.Default.DirectionsWalk,
              onClick = onNavigateToSafeJourney,
              modifier = Modifier.weight(1f).testTag("btn_safe_journey")
            )
            SafetyShortcut(
              title = if (isBangla) "ফেক কল" else "Fake call",
              icon = Icons.Default.PhoneCallback,
              onClick = onNavigateToFakeCall,
              modifier = Modifier.weight(1f).testTag("btn_fake_call")
            )
          }
          Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            SafetyShortcut(
              title = if (isBangla) "চেক-ইন টাইমার" else "Check-in timer",
              icon = Icons.Default.Timer,
              onClick = onNavigateToSafetyTimer,
              modifier = Modifier.weight(1f).testTag("btn_safety_timer")
            )
            SafetyShortcut(
              title = if (isBangla) "সহায়তার স্থান" else "Safe places",
              icon = Icons.Default.Place,
              onClick = onNavigateToSafePlaces,
              modifier = Modifier.weight(1f).testTag("btn_safe_places")
            )
          }
        }

        if (!guideComplete) {
          SafetyActionRow(
            title = if (isBangla) "RAMISA ব্যবহার শিখুন" else "Learn the safety basics",
            icon = Icons.Default.HelpOutline,
            onClick = { showGuide = true },
            modifier = Modifier.testTag("btn_guide_teaser")
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isBangla) "নিরাপত্তার অবস্থা" else "Safety status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Surface(
            modifier = Modifier.fillMaxWidth().testTag("safety_status_card"),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth().testTag("card_live_gps_telemetry"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val locationSubtitle = when {
                  state.isRefreshingLocation -> if (isBangla) "অবস্থান খোঁজা হচ্ছে…" else "Requesting device location…"
                  !EmergencyLocationManager.hasLocationPermission(context) ->
                    if (isBangla) "অবস্থানের অনুমতি দিন" else "Allow location access to get a fix"
                  !state.isGpsHardwareEnabled -> if (isBangla) "ডিভাইসের অবস্থান বন্ধ" else "Device location is off"
                  else -> state.currentLocation?.description()
                    ?: if (isBangla) "এখনও অবস্থান পাওয়া যায়নি" else "Waiting for a device location fix"
                }
                SafetyActionRow(
                  title = if (isBangla) "নিরাপত্তা মানচিত্র" else "Safety map",
                  subtitle = locationSubtitle,
                  icon = Icons.Default.Place,
                  onClick = onNavigateToDangerMap,
                  modifier = Modifier.weight(1f).testTag("card_danger_map"),
                  iconColor = if (state.isLocationReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                  showArrow = false
                )
                IconButton(
                  onClick = {
                    if (EmergencyLocationManager.hasLocationPermission(context)) {
                      viewModel.refreshLocation(context)
                    } else {
                      locationPermission.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                      )
                    }
                  },
                  enabled = !state.isRefreshingLocation,
                  modifier = Modifier.padding(end = 8.dp).testTag("btn_refresh_gps")
                ) {
                  Icon(
                    Icons.Default.Refresh,
                    contentDescription = if (isBangla) "অবস্থান আবার খুঁজুন" else "Refresh device location"
                  )
                }
              }
              HorizontalDivider()
              SafetyActionRow(
                title = if (isBangla) "ডিভাইস শর্টকাট" else "Device shortcuts",
                subtitle = if (isBangla) "ফোন ঝাঁকানো ও বাটনের শর্টকাট" else "Shake and button settings",
                icon = Icons.Default.Sensors,
                onClick = onNavigateToHardwareTrigger
              )
              HorizontalDivider()
              SafetyActionRow(
                title = if (isBangla) "কমিউনিটি নিরাপত্তা" else "Community safety",
                subtitle = latestAlert?.let { "${it.triggerTypeLabel} · ${it.locationName}" }
                  ?: if (isBangla) "ফোরাম ও এলাকার রিপোর্ট" else "Forum and area reports",
                icon = if (latestAlert == null) Icons.Default.Group else Icons.Default.Warning,
                iconColor = if (latestAlert == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                onClick = onNavigateToCommunitySafety,
                modifier = Modifier.testTag("card_community_network_home")
              )
              HorizontalDivider()
              SafetyActionRow(
                title = if (isBangla) "ঘটনার ইতিহাস" else "Incident history",
                icon = Icons.Default.History,
                onClick = onNavigateToHistory
              )
            }
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth().testTag("hotline_info_bar"),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = if (isBangla) "বাংলাদেশের হটলাইন" else "Bangladesh hotlines",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val hotlines = listOf(
              BangladeshHotlines.EMERGENCY to if (isBangla) "জরুরি সেবা" else "Emergency",
              BangladeshHotlines.VIOLENCE_SUPPORT to if (isBangla) "নির্যাতন প্রতিরোধ" else "Violence support",
              BangladeshHotlines.CHILD_HELP to if (isBangla) "শিশু সহায়তা" else "Child helpline"
            )
            hotlines.forEach { (number, label) ->
              Surface(
                onClick = {
                  if (!BangladeshHotlines.openDialer(context, number)) {
                    Toast.makeText(context, "$label $number", Toast.LENGTH_LONG).show()
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(number, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                  Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }
              }
            }
          }
        }
      }
    }
  }

  if (showGuide) {
    RamisaGuideSheet(
      isBangla = isBangla,
      onDismiss = { showGuide = false },
      onComplete = {
        guideComplete = true
        preferences.edit().putInt("completed_version", 1).apply()
        showGuide = false
      }
    )
  }
}

@Composable
private fun DashboardHeader(isBangla: Boolean, onNavigateToProfile: () -> Unit, onOpenGuide: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Image(
      painter = painterResource(R.drawable.img_app_logo),
      contentDescription = null,
      modifier = Modifier.size(36.dp).clip(CircleShape)
    )
    Text(
      text = "RAMISA",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f).testTag("app_logo_title")
    )
    TextButton(
      onClick = { AppSessionManager.toggleLanguage() },
      modifier = Modifier.testTag("btn_language_toggle")
    ) {
      Text(if (isBangla) "English" else "বাংলা")
    }
    IconButton(onClick = onOpenGuide, modifier = Modifier.testTag("btn_help_guide")) {
      Icon(Icons.Default.HelpOutline, contentDescription = if (isBangla) "ব্যবহারের গাইড" else "Safety guide")
    }
    FilledTonalIconButton(
      onClick = onNavigateToProfile,
      modifier = Modifier.testTag("btn_home_profile")
    ) {
      Icon(Icons.Default.Person, contentDescription = if (isBangla) "প্রোফাইল" else "Profile")
    }
  }
}

@Composable
private fun ContactsReadiness(state: HomeUiState, isBangla: Boolean, onNavigateToContacts: () -> Unit) {
  val count = state.activeContactCount
  val status = when {
    state.contactsUnavailable -> if (isBangla) "পরিচিতি লোড করা যায়নি" else "Contacts couldn’t load"
    count == null -> if (isBangla) "পরিচিতি দেখা হচ্ছে" else "Checking contacts"
    count == 0 -> if (isBangla) "বিশ্বস্ত পরিচিতি যোগ করুন" else "Set up trusted contacts"
    count == 1 -> if (isBangla) "SMS-এর জন্য ১ জন পরিচিতি" else "1 contact enabled for SOS SMS"
    else -> if (isBangla) "SMS-এর জন্য ${count} জন পরিচিতি" else "${count} contacts enabled for SOS SMS"
  }
  Surface(
    onClick = onNavigateToContacts,
    modifier = Modifier.fillMaxWidth().testTag("card_contacts"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(status, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
      Text(
        if (count == 0) { if (isBangla) "যোগ করুন" else "Set up" }
        else { if (isBangla) "দেখুন" else "View" },
        style = MaterialTheme.typography.labelLarge
      )
    }
  }
}

@Composable
private fun EmergencyPanel(isBangla: Boolean, onTriggerSos: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().testTag("sos_button_container"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
      Button(
        onClick = onTriggerSos,
        modifier = Modifier.sizeIn(minWidth = 164.dp, minHeight = 164.dp).testTag("sos_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(26.dp))
          Text("SOS", fontSize = 40.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
      }
      Text(
        if (isBangla) "SMS ও কলের বিকল্প দেখুন" else "Review SMS and call options",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
  }
}

@Composable
private fun SafetyShortcut(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier.heightIn(min = 92.dp).fillMaxHeight(),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
      Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
  }
}
