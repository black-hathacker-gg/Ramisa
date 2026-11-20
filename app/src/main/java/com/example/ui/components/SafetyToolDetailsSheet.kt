package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyToolDetailsSheet(
  route: Screen,
  title: String,
  description: String,
  icon: ImageVector,
  isBangla: Boolean,
  onDismiss: () -> Unit,
  onOpen: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        .testTag("tool_details_sheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
      }
      Text(description, style = MaterialTheme.typography.bodyLarge)
      toolDetails[route]?.let { (english, bangla) ->
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            if (isBangla) bangla else english,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(14.dp)
          )
        }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
          Text(if (isBangla) "বন্ধ করুন" else "Close")
        }
        Button(onClick = onOpen, modifier = Modifier.heightIn(min = 48.dp).testTag("btn_open_tool_details")) {
          Text(if (isBangla) "টুল খুলুন" else "Open tool")
        }
      }
    }
  }
}

private val toolDetails = mapOf(
  Screen.SafeJourney to (
    "Enter the starting point, destination and expected travel time. The journey and check-in run on this device while its screen is open." to
      "শুরুর স্থান, গন্তব্য ও যাত্রার সময় লিখুন। এই স্ক্রিন খোলা থাকলে এই ডিভাইসে যাত্রা ও চেক-ইন চলে।"
  ),
  Screen.FakeCall to (
    "Choose a caller and a delay. This simulates an incoming call on your phone and may ring or vibrate." to
      "কলার ও সময় বেছে নিন। এটি ফোনে আসা কলের মতো দেখায় এবং রিং বা কম্পন হতে পারে।"
  ),
  Screen.SafetyTimer to (
    "Set a deadline and keep the timer screen open. A missed check-in attempts to send SMS to eligible contacts when SMS access and cellular service are available." to
      "সময় ঠিক করে টাইমার স্ক্রিন খোলা রাখুন। চেক-ইন মিস হলে, অনুমতি ও মোবাইল সেবা থাকলে যোগ্য পরিচিতিদের SMS পাঠানোর চেষ্টা হবে।"
  ),
  Screen.RideSafety to (
    "Enter the vehicle and trip details you want to share. Choose where to send them using the Android share menu." to
      "গাড়ি ও যাত্রার যে তথ্য জানাতে চান তা লিখুন। অ্যান্ড্রয়েডের শেয়ার মেনুতে প্রাপক বেছে নিন।"
  ),
  Screen.EmergencyContacts to (
    "Check each number and choose SMS and call options. A saved contact is eligible for SOS SMS only if SMS is enabled and the number is usable." to
      "প্রতিটি নম্বর দেখে SMS ও কলের বিকল্প ঠিক করুন। সঠিক নম্বর ও SMS চালু থাকলে পরিচিতিকে SOS SMS পাঠানো যায়।"
  ),
  Screen.ThreatGuard to (
    "Allow microphone access to analyze sounds on the device. Detection can miss sounds or misclassify them. A confirmed prompt opens SOS review." to
      "ডিভাইসেই শব্দ বিশ্লেষণের জন্য মাইক্রোফোনের অনুমতি দিন। শনাক্তকরণ ভুল হতে পারে। নিশ্চিত করলে SOS পর্যালোচনা খুলবে।"
  ),
  Screen.SirenStrobe to (
    "Choose sound or flash deliberately. Either can draw attention nearby. You can stop playback from the same screen." to
      "পরিস্থিতি দেখে শব্দ বা আলো বেছে নিন। এতে আশপাশের মনোযোগ আকৃষ্ট হতে পারে। একই স্ক্রিন থেকে বন্ধ করতে পারবেন।"
  ),
  Screen.HardwareTrigger to (
    "Check which triggers your device supports. Review any automatic action before enabling the shortcut." to
      "আপনার ডিভাইসে কোন ট্রিগার চলে দেখুন। শর্টকাট চালুর আগে স্বয়ংক্রিয় কাজগুলো পরীক্ষা করুন।"
  ),
  Screen.LockScreenSos to (
    "Android notification access is needed for the lock-screen shortcut. Direct SMS has a separate opt-in and requires SMS permission." to
      "লকস্ক্রিন শর্টকাটে অ্যান্ড্রয়েড নোটিফিকেশনের অনুমতি দরকার। সরাসরি SMS আলাদা করে চালু করতে হয়।"
  ),
  Screen.StealthMode to (
    "Set and remember your safety PIN before using the calculator disguise. Android settings and notifications can still reveal the app." to
      "ক্যালকুলেটরের ছদ্মবেশ ব্যবহার করার আগে নিরাপত্তা PIN ঠিক করে মনে রাখুন। অ্যান্ড্রয়েড সেটিংস বা নোটিফিকেশনে অ্যাপ দেখা যেতে পারে।"
  ),
  Screen.MeshNetwork to (
    "This tool is a local simulation. Use it to see the relay workflow without contacting nearby devices or sending an SOS." to
      "এটি ডিভাইসের অনুশীলন। পাশের ডিভাইসে যোগাযোগ বা SOS না পাঠিয়েই রিলে পদ্ধতি দেখুন।"
  ),
  Screen.BatteryPing to (
    "Preview the alert before enabling it. Live SMS needs eligible contacts, SMS permission and cellular service. Location is included when available." to
      "চালুর আগে অ্যালার্টের নমুনা দেখুন। SMS-এর জন্য যোগ্য পরিচিতি, অনুমতি ও মোবাইল সেবা দরকার। অবস্থান পাওয়া গেলে তা যোগ হবে।"
  ),
  Screen.SafePlaces to (
    "Call ahead or check local information before relying on a place’s hours, access or services." to
      "কোনো স্থানের সময়, প্রবেশাধিকার ও সেবা ব্যবহারের আগে ফোন করে বা স্থানীয় তথ্য থেকে যাচাই করুন।"
  ),
  Screen.DangerMap to (
    "Street tiles come from OpenStreetMap. Example entries and reports are not current safety assessments." to
      "রাস্তার মানচিত্র OpenStreetMap থেকে আসে। নমুনা ও রিপোর্ট কোনো স্থানের বর্তমান নিরাপত্তা নিশ্চিত করে না।"
  ),
  Screen.OfflineMaps to (
    "Download an area before leaving internet coverage. Saved tiles help with navigation. New reports and map updates require a connection." to
      "ইন্টারনেট ছাড়ার আগে এলাকার মানচিত্র ডাউনলোড করুন। নতুন রিপোর্ট বা মানচিত্র আপডেট পেতে ইন্টারনেট দরকার।"
  ),
  Screen.CommunitySafety to (
    "Posting to the shared community needs a signed-in account and a connection. Reports can be unverified, so avoid including identifying details." to
      "কমিউনিটিতে পোস্ট করতে সাইন-ইন ও ইন্টারনেট দরকার। রিপোর্ট যাচাই নাও হতে পারে, তাই ব্যক্তিগত পরিচয় দেবেন না।"
  ),
  Screen.VolunteerEscort to (
    "Explore the escort workflow using example information. RAMISA does not arrange an escort through this preview." to
      "নমুনা দিয়ে সঙ্গী খোঁজার পদ্ধতি দেখুন। এই অনুশীলন থেকে RAMISA কোনো সঙ্গীর ব্যবস্থা করে না।"
  ),
  Screen.SafetyGuide to (
    "Browse the safety guides by topic. Local conditions and advice can change." to
      "বিষয় অনুযায়ী নিরাপত্তা নির্দেশিকা পড়ুন। স্থানীয় পরিস্থিতি ও পরামর্শ বদলাতে পারে।"
  ),
  Screen.LegalAid to (
    "Prepare a draft and review it before sharing it with a qualified provider." to
      "খসড়া তৈরি করুন এবং উপযুক্ত সেবাদাতাকে দেওয়ার আগে পড়ে দেখুন।"
  ),
  Screen.CyberCrimeSupport to (
    "Keep personal information and sensitive evidence private. Use the listed reporting contacts to ask for official help." to
      "ব্যক্তিগত তথ্য ও সংবেদনশীল প্রমাণ গোপনে রাখুন। সরকারি সাহায্যের জন্য দেওয়া যোগাযোগের তথ্য ব্যবহার করুন।"
  ),
  Screen.FirstAid to (
    "Open the offline reference by topic. In an emergency, seek qualified medical help." to
      "বিষয় অনুযায়ী অফলাইন নির্দেশিকা খুলুন। জরুরি অবস্থায় যোগ্য চিকিৎসকের সহায়তা নিন।"
  ),
  Screen.WellnessCounseling to (
    "Browse recovery exercises and find listed support organizations. Services and availability can vary." to
      "সুস্থতার অনুশীলন ও সহায়তা প্রতিষ্ঠানের তথ্য দেখুন। সেবা ও প্রাপ্যতা বদলাতে পারে।"
  ),
  Screen.SelfDefense to (
    "Read escape-focused safety guidance and seek qualified instruction for physical skills." to
      "নিরাপদে সরে যাওয়ার নির্দেশনা পড়ুন এবং শারীরিক কৌশল শেখার জন্য যোগ্য প্রশিক্ষক খুঁজুন।"
  ),
  Screen.History to (
    "Read each recipient’s outcome separately. A sent status records modem acceptance. A delivered status needs a carrier receipt." to
      "প্রতি প্রাপকের অবস্থা আলাদা দেখুন। ‘পাঠানো’ মানে ডিভাইস গ্রহণ করেছে। ‘পৌঁছেছে’ দেখাতে অপারেটরের রসিদ দরকার।"
  )
)
