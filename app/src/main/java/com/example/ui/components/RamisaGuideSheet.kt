package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamisaGuideSheet(isBangla: Boolean, onDismiss: () -> Unit, onComplete: () -> Unit) {
  var step by rememberSaveable { mutableIntStateOf(0) }
  var practiced by rememberSaveable { mutableStateOf(false) }
  val steps = if (isBangla) {
    listOf(
      "বিশ্বস্ত পরিচিতি" to "জরুরি অবস্থায় যাঁদের জানাতে চান, তাঁদের যোগ করুন। প্রতিটি পরিচিতির জন্য SMS বা কলের বিকল্প ঠিক করুন।",
      "SOS অনুশীলন" to "বাস্তবে SOS খুললে SMS ও কলের বিকল্প দেখতে পাবেন। এখানে নিরাপদে অনুশীলন করুন।",
      "যাত্রা ও চেক-ইন" to "নিরাপদ যাত্রা বা চেক-ইন টাইমার ব্যবহার করুন। প্রতিটি টুলের তথ্য বোতামে কীভাবে কাজ করে তা দেখুন।"
    )
  } else {
    listOf(
      "Trusted contacts" to "Add people you trust and choose whether each can receive SOS SMS or appear in your call options.",
      "Practice SOS" to "An SOS opens your emergency options. Try the practice button below to preview the decision without sending a message.",
      "Journeys and check-ins" to "Set up a Safe Journey or check-in timer before leaving. Open a tool’s Details for prerequisites and limits."
    )
  }
  val (title, body) = steps[step]

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        .testTag("ramisa_guide_sheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      BoxWithConstraints(Modifier.fillMaxWidth()) {
        val showIllustration = maxWidth >= 300.dp
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          if (showIllustration) {
            Image(painterResource(R.drawable.ramisa_guide), null, Modifier.size(width = 76.dp, height = 118.dp))
          }
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(body, style = MaterialTheme.typography.bodyLarge)
          }
        }
      }
      if (step == 1) {
        FilledTonalButton(
          onClick = { practiced = true },
          modifier = Modifier.heightIn(min = 48.dp).testTag("guide_practice_sos")
        ) {
          Text(if (isBangla) "অনুশীলন দেখুন" else "Preview practice alert")
        }
        if (practiced) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              if (isBangla) "অনুশীলন শেষ। কোনো SMS বা কল যায়নি।" else "Practice complete. No SMS or call was started.",
              modifier = Modifier.padding(12.dp).testTag("guide_practice_result"),
              style = MaterialTheme.typography.bodyLarge
            )
          }
        }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onComplete, modifier = Modifier.heightIn(min = 48.dp)) {
          Text(if (isBangla) "বাদ দিন" else "Skip")
        }
        FilledTonalButton(
          onClick = { if (step == steps.lastIndex) onComplete() else step++ },
          modifier = Modifier.heightIn(min = 48.dp).testTag("guide_next")
        ) {
          Text(
            if (step == steps.lastIndex) { if (isBangla) "শেষ" else "Done" }
            else { if (isBangla) "পরবর্তী" else "Next" }
          )
        }
      }
    }
  }
}
