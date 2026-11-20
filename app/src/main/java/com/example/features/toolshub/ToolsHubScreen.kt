package com.example.features.toolshub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.navigation.Screen
import com.example.data.repository.AppSessionManager
import com.example.ui.components.SafetyActionRow
import com.example.ui.components.SafetyToolDetailsSheet
import kotlinx.coroutines.launch

private data class ToolText(val english: String, val bangla: String) {
  fun text(isBangla: Boolean): String = if (isBangla) bangla else english
}

private enum class ToolGroup(val label: ToolText) {
  EVERYDAY(ToolText("Everyday", "দৈনন্দিন")),
  PROTECTION(ToolText("Protection", "সুরক্ষা")),
  SUPPORT(ToolText("Support", "সহায়তা"))
}

private data class SafetyTool(
  val route: Screen,
  val title: ToolText,
  val subtitle: ToolText,
  val icon: ImageVector,
  val testTag: String
) {
  fun matches(query: String): Boolean {
    val labels = "${title.english} ${title.bangla} ${subtitle.english} ${subtitle.bangla}"
    return query.split(' ').all { word -> labels.contains(word, ignoreCase = true) }
  }
}

private val toolsByGroup = mapOf(
  ToolGroup.EVERYDAY to listOf(
    SafetyTool(Screen.SafeJourney, ToolText("Safe journey", "নিরাপদ যাত্রা"),
      ToolText("Track a trip and check in on arrival", "যাত্রা ট্র্যাক করুন ও পৌঁছে জানান"),
      Icons.Default.DirectionsWalk, "tool_safe_journey"),
    SafetyTool(Screen.FakeCall, ToolText("Fake call", "ফেক কল"),
      ToolText("Schedule a simulated incoming call", "নকল ইনকামিং কলের সময় ঠিক করুন"),
      Icons.Default.PhoneCallback, "tool_fake_call"),
    SafetyTool(Screen.SafetyTimer, ToolText("Safety timer", "নিরাপত্তা টাইমার"),
      ToolText("Set a check-in deadline", "চেক-ইনের সময়সীমা ঠিক করুন"),
      Icons.Default.Timer, "tool_safety_timer"),
    SafetyTool(Screen.RideSafety, ToolText("Ride safety", "যাত্রার নিরাপত্তা"),
      ToolText("Record rickshaw, vehicle and trip details", "রিকশা, গাড়ি ও যাত্রার তথ্য রাখুন"),
      Icons.Default.DirectionsWalk, "tool_ride_safety")
  ),
  ToolGroup.PROTECTION to listOf(
    SafetyTool(Screen.EmergencyContacts, ToolText("Emergency contacts", "জরুরি পরিচিতি"),
      ToolText("Manage trusted contacts and SMS recipients", "বিশ্বস্ত পরিচিতি ও SMS প্রাপক ঠিক করুন"),
      Icons.Default.Contacts, "tool_contacts"),
    SafetyTool(Screen.ThreatGuard, ToolText("AI Threat Guard", "এআই থ্রেট গার্ড"),
      ToolText("Monitor for distress sounds on your device", "ডিভাইসে বিপদের শব্দ শনাক্ত করুন"),
      Icons.Default.Sensors, "tool_threat_guard"),
    SafetyTool(Screen.SirenStrobe, ToolText("Siren & strobe", "সাইরেন ও আলো"),
      ToolText("Play an alarm and flash the device light", "অ্যালার্ম বাজান ও ফ্ল্যাশ চালান"),
      Icons.Default.VolumeUp, "tool_siren_strobe"),
    SafetyTool(Screen.HardwareTrigger, ToolText("Hardware SOS", "হার্ডওয়্যার SOS"),
      ToolText("View shake and button shortcuts", "ফোন ঝাঁকানো ও বাটনের শর্টকাট দেখুন"),
      Icons.Default.FlashOn, "tool_hardware_trigger"),
    SafetyTool(Screen.LockScreenSos, ToolText("Lock screen SOS", "লকস্ক্রিন SOS"),
      ToolText("Set up an SOS shortcut from the lock screen", "লকস্ক্রিন থেকে SOS শর্টকাট সেট করুন"),
      Icons.Default.Lock, "tool_lockscreen_sos"),
    SafetyTool(Screen.StealthMode, ToolText("Stealth calculator", "গোপন ক্যালকুলেটর"),
      ToolText("Open the discreet keypad and SOS shortcut", "গোপন কীপ্যাড ও SOS শর্টকাট খুলুন"),
      Icons.Default.Lock, "tool_stealth_mode"),
    SafetyTool(Screen.MeshNetwork, ToolText("Offline mesh", "অফলাইন মেশ"),
      ToolText("Try an on-device relay simulation", "ডিভাইসে রিলের অনুশীলন দেখুন"),
      Icons.Default.Hub, "tool_mesh_network"),
    SafetyTool(Screen.BatteryPing, ToolText("Low-battery auto-ping", "কম ব্যাটারি সতর্কতা"),
      ToolText("Manage the low-battery location alert", "কম ব্যাটারিতে অবস্থানের সতর্কতা পরিচালনা করুন"),
      Icons.Default.BatteryAlert, "tool_battery_ping")
  ),
  ToolGroup.SUPPORT to listOf(
    SafetyTool(Screen.SafePlaces, ToolText("Safe places", "নিরাপদ স্থান"),
      ToolText("Browse police stations, hospitals and support locations", "থানা, হাসপাতাল ও সহায়তা কেন্দ্র দেখুন"),
      Icons.Default.Place, "tool_safe_places"),
    SafetyTool(Screen.DangerMap, ToolText("Safety map", "নিরাপত্তা মানচিত্র"),
      ToolText("View reported danger zones and nearby areas", "ঘটনার রিপোর্ট ও আশপাশের এলাকা দেখুন"),
      Icons.Default.Map, "tool_danger_map"),
    SafetyTool(Screen.OfflineMaps, ToolText("Offline maps", "অফলাইন মানচিত্র"),
      ToolText("Manage saved map regions", "সংরক্ষিত মানচিত্র অঞ্চল পরিচালনা করুন"),
      Icons.Default.Map, "tool_offline_maps"),
    SafetyTool(Screen.CommunitySafety, ToolText("Community safety", "কমিউনিটি নিরাপত্তা"),
      ToolText("Forum, area reports and alerts", "ফোরাম, এলাকার রিপোর্ট ও সতর্কতা"),
      Icons.Default.Group, "tool_community_safety"),
    SafetyTool(Screen.VolunteerEscort, ToolText("Volunteer escort", "স্বেচ্ছাসেবী সহায়তা"),
      ToolText("Find peer escort information", "সহযাত্রীর সহায়তার তথ্য দেখুন"),
      Icons.Default.Group, "tool_volunteer_escort"),
    SafetyTool(Screen.SafetyGuide, ToolText("Safety guide & rights", "নিরাপত্তা নির্দেশিকা ও অধিকার"),
      ToolText("Read safety guidance and legal rights", "নিরাপত্তা নির্দেশিকা ও আইনি অধিকার পড়ুন"),
      Icons.AutoMirrored.Filled.Assignment, "tool_legal_guide"),
    SafetyTool(Screen.LegalAid, ToolText("Legal aid & GD", "আইনি সহায়তা ও জিডি"),
      ToolText("Find legal support and draft a general diary", "আইনি সহায়তা খুঁজুন ও জিডির খসড়া লিখুন"),
      Icons.Default.Gavel, "tool_legal_aid"),
    SafetyTool(Screen.CyberCrimeSupport, ToolText("Cyber harassment support", "সাইবার হয়রানিতে সহায়তা"),
      ToolText("Reporting resources and cybercrime contacts", "রিপোর্টের উপায় ও সাইবার অপরাধের যোগাযোগ"),
      Icons.Default.Security, "tool_cyber_crime"),
    SafetyTool(Screen.FirstAid, ToolText("First aid", "প্রাথমিক চিকিৎসা"),
      ToolText("Read the offline first-aid guide", "অফলাইন প্রাথমিক চিকিৎসার নির্দেশিকা পড়ুন"),
      Icons.Default.LocalHospital, "tool_first_aid"),
    SafetyTool(Screen.WellnessCounseling, ToolText("Wellness & counseling", "সুস্থতা ও কাউন্সেলিং"),
      ToolText("Find recovery and counseling resources", "মানসিক সহায়তা ও কাউন্সেলিংয়ের তথ্য খুঁজুন"),
      Icons.Default.Spa, "tool_wellness"),
    SafetyTool(Screen.SelfDefense, ToolText("Self-defense guides", "আত্মরক্ষার নির্দেশিকা"),
      ToolText("Browse personal-safety guidance", "ব্যক্তিগত নিরাপত্তার নির্দেশিকা পড়ুন"),
      Icons.Default.SportsMartialArts, "tool_self_defense"),
    SafetyTool(Screen.History, ToolText("Incident history", "ঘটনার ইতিহাস"),
      ToolText("Review saved events and SMS status", "সংরক্ষিত ঘটনা ও SMS-এর অবস্থা দেখুন"),
      Icons.Default.History, "tool_history")
  )
)

@Composable
fun ToolsHubScreen(onNavigate: (Screen) -> Unit, onTriggerSos: () -> Unit) {
  val isBangla by AppSessionManager.isBanglaLanguage.collectAsStateWithLifecycle()
  var query by rememberSaveable { mutableStateOf("") }
  var selectedGroup by rememberSaveable { mutableStateOf<ToolGroup?>(null) }
  var detailsRoute by rememberSaveable { mutableStateOf<String?>(null) }
  val listState = rememberLazyListState()
  val focusManager = LocalFocusManager.current
  val scrollScope = rememberCoroutineScope()
  val matchingGroups = toolsByGroup
    .filterKeys { selectedGroup == null || selectedGroup == it }
    .mapValues { (_, tools) -> tools.filter { it.matches(query.trim()) } }
    .filterValues { it.isNotEmpty() }

  fun resetListPosition() {
    scrollScope.launch { listState.scrollToItem(0) }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().imePadding()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isBangla) "নিরাপত্তা টুল" else "Safety tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f).semantics { heading() }
          )
          FilledTonalButton(onClick = onTriggerSos, modifier = Modifier.testTag("tools_sos")) {
            Text("SOS", fontWeight = FontWeight.SemiBold)
          }
        }
        OutlinedTextField(
          value = query,
          onValueChange = {
            query = it
            selectedGroup = null
            resetListPosition()
          },
          label = { Text(if (isBangla) "টুল খুঁজুন" else "Search tools") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = {
                query = ""
                resetListPosition()
              }) {
                Icon(Icons.Default.Close, contentDescription = if (isBangla) "লেখা মুছুন" else "Clear search")
              }
            }
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).testTag("search_tools"),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(listOf(null) + ToolGroup.entries, key = { it?.name ?: "ALL" }) { group ->
            FilterChip(
              selected = selectedGroup == group,
              onClick = {
                selectedGroup = group
                focusManager.clearFocus()
                resetListPosition()
              },
              label = { Text(group?.label?.text(isBangla) ?: if (isBangla) "সব" else "All") },
              modifier = Modifier.heightIn(min = 48.dp)
            )
          }
        }
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxWidth().weight(1f),
          contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          if (matchingGroups.isEmpty()) {
            item {
              Text(
                text = if (isBangla) "কোনো টুল পাওয়া যায়নি" else "No matching tools",
                modifier = Modifier.padding(vertical = 20.dp),
                style = MaterialTheme.typography.bodyLarge
              )
            }
          }
          matchingGroups.forEach { (group, tools) ->
            item(key = group.name) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = group.label.text(isBangla),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                  Column {
                    tools.forEachIndexed { index, tool ->
                      if (index > 0) HorizontalDivider()
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        SafetyActionRow(
                          title = tool.title.text(isBangla),
                          icon = tool.icon,
                          onClick = { onNavigate(tool.route) },
                          modifier = Modifier.weight(1f).heightIn(min = 72.dp).testTag(tool.testTag)
                        )
                        IconButton(
                          onClick = { detailsRoute = tool.route.route },
                          modifier = Modifier.padding(end = 4.dp).testTag("details_${tool.route.route}")
                        ) {
                          Icon(
                            Icons.Default.Info,
                            contentDescription = if (isBangla) "${tool.title.bangla}: বিস্তারিত"
                              else "${tool.title.english} details"
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  val selectedTool = toolsByGroup.values.asSequence().flatten().firstOrNull {
    it.route.route == detailsRoute
  }
  if (selectedTool != null) {
    SafetyToolDetailsSheet(
      route = selectedTool.route,
      title = selectedTool.title.text(isBangla),
      description = selectedTool.subtitle.text(isBangla),
      icon = selectedTool.icon,
      isBangla = isBangla,
      onDismiss = { detailsRoute = null },
      onOpen = {
        detailsRoute = null
        onNavigate(selectedTool.route)
      }
    )
  }
}
