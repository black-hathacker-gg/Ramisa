package com.example.features.stealth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkContainer
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StealthModeScreen(
  viewModel: StealthModeViewModel,
  onTriggerSilentSos: () -> Unit,
  onExitStealthToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showHintSheet by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Calculator",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
          )
        },
        actions = {
          IconButton(
            onClick = { showHintSheet = !showHintSheet },
            modifier = Modifier.testTag("btn_stealth_info")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Stealth Instructions",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Hints Dropdown (for testing and user awareness)
      AnimatedVisibility(visible = showHintSheet) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("stealth_hint_card"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = PinkContainer)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = "CAMOUFLAGE STEALTH ENGINE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimaryDark
              )
            }
            Text(
              text = "• Enter '9999' and tap '=' for Silent SOS Broadcast",
              style = MaterialTheme.typography.bodySmall,
              color = PinkPrimaryDark
            )
            Text(
              text = "• Enter '1234' and tap '=' to return to RAMISA Home",
              style = MaterialTheme.typography.bodySmall,
              color = PinkPrimaryDark
            )
          }
        }
      }

      // Display Screen
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(vertical = 12.dp)
          .testTag("calc_display_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.Bottom
        ) {
          if (uiState.formulaText.isNotEmpty()) {
            Text(
              text = uiState.formulaText,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          Text(
            text = uiState.displayText,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.testTag("calc_display_text")
          )

          if (uiState.isSecretSosTriggered) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PinkContainer
            ) {
              Text(
                text = "✓ Background SOS Signal Sent",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PinkPrimaryDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // Keypad Layout (Pink & White Minimalist Aesthetics)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Row 1: C, ( ), %, ÷
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CalcKey(text = "C", isSpecial = true, onClick = { viewModel.onClear() }, modifier = Modifier.weight(1f), testTag = "key_clear")
          CalcKey(text = "±", isSpecial = true, onClick = { /* no-op */ }, modifier = Modifier.weight(1f), testTag = "key_plus_minus")
          CalcKey(text = "%", isSpecial = true, onClick = { /* no-op */ }, modifier = Modifier.weight(1f), testTag = "key_percent")
          CalcKey(text = "÷", isOperator = true, onClick = { viewModel.onOperator("÷") }, modifier = Modifier.weight(1f), testTag = "key_div")
        }

        // Row 2: 7, 8, 9, ×
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CalcKey(text = "7", onClick = { viewModel.onDigit("7") }, modifier = Modifier.weight(1f), testTag = "key_7")
          CalcKey(text = "8", onClick = { viewModel.onDigit("8") }, modifier = Modifier.weight(1f), testTag = "key_8")
          CalcKey(text = "9", onClick = { viewModel.onDigit("9") }, modifier = Modifier.weight(1f), testTag = "key_9")
          CalcKey(text = "×", isOperator = true, onClick = { viewModel.onOperator("×") }, modifier = Modifier.weight(1f), testTag = "key_mult")
        }

        // Row 3: 4, 5, 6, -
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CalcKey(text = "4", onClick = { viewModel.onDigit("4") }, modifier = Modifier.weight(1f), testTag = "key_4")
          CalcKey(text = "5", onClick = { viewModel.onDigit("5") }, modifier = Modifier.weight(1f), testTag = "key_5")
          CalcKey(text = "6", onClick = { viewModel.onDigit("6") }, modifier = Modifier.weight(1f), testTag = "key_6")
          CalcKey(text = "-", isOperator = true, onClick = { viewModel.onOperator("-") }, modifier = Modifier.weight(1f), testTag = "key_minus")
        }

        // Row 4: 1, 2, 3, +
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CalcKey(text = "1", onClick = { viewModel.onDigit("1") }, modifier = Modifier.weight(1f), testTag = "key_1")
          CalcKey(text = "2", onClick = { viewModel.onDigit("2") }, modifier = Modifier.weight(1f), testTag = "key_2")
          CalcKey(text = "3", onClick = { viewModel.onDigit("3") }, modifier = Modifier.weight(1f), testTag = "key_3")
          CalcKey(text = "+", isOperator = true, onClick = { viewModel.onOperator("+") }, modifier = Modifier.weight(1f), testTag = "key_plus")
        }

        // Row 5: 0, ., =
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CalcKey(text = "0", onClick = { viewModel.onDigit("0") }, modifier = Modifier.weight(2f), testTag = "key_0")
          CalcKey(text = ".", onClick = { viewModel.onDecimal() }, modifier = Modifier.weight(1f), testTag = "key_dot")
          CalcKey(
            text = "=",
            isPrimary = true,
            onClick = {
              viewModel.onEquals(
                onSecretSos = onTriggerSilentSos,
                onUnlockApp = onExitStealthToHome
              )
            },
            modifier = Modifier.weight(1f),
            testTag = "key_equals"
          )
        }
      }
    }
  }
}

@Composable
private fun CalcKey(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isOperator: Boolean = false,
  isSpecial: Boolean = false,
  isPrimary: Boolean = false,
  testTag: String = ""
) {
  val bgColor = when {
    isPrimary -> PinkPrimary
    isOperator -> PinkContainer
    isSpecial -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surface
  }

  val textColor = when {
    isPrimary -> Color.White
    isOperator -> PinkPrimaryDark
    isSpecial -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.onSurface
  }

  Surface(
    modifier = modifier
      .height(64.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    color = bgColor,
    shadowElevation = if (isPrimary) 4.dp else 1.dp
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = if (isPrimary || isOperator) FontWeight.Bold else FontWeight.Medium,
          fontSize = 22.sp
        ),
        color = textColor
      )
    }
  }
}
