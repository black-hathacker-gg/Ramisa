package com.example.features.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun DangerMapHomeWidget(
  onOpenFullMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedCard(
    onClick = onOpenFullMap,
    modifier = modifier.testTag("card_danger_map_home_widget")
  ) {
    ListItem(
      headlineContent = { Text("Safety map") },
      supportingContent = { Text("View risk pins, bypass advice and your saved reports.") },
      leadingContent = { Icon(Icons.Default.Map, contentDescription = null) },
      modifier = Modifier.padding(vertical = 4.dp).testTag("btn_open_danger_map_full")
    )
  }
}
