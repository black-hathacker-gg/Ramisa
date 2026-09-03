package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.sms.EmergencySmsDispatcher
import com.example.core.sms.SmsDispatchReport
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import kotlinx.coroutines.delay

/**
 * Top floating banner that displays an unmistakable green confirmation prompt
 * whenever an offline SMS is dispatched to emergency contacts.
 */
@Composable
fun GlobalSmsConfirmationBanner(
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    val dispatchEvent by EmergencySmsDispatcher.lastDispatchEvent.collectAsStateWithLifecycle()
    var visibleReport by remember { mutableStateOf<SmsDispatchReport?>(null) }

    LaunchedEffect(dispatchEvent) {
        val event = dispatchEvent
        if (event != null) {
            visibleReport = event
            // Auto dismiss after 5 seconds
            delay(5000)
            visibleReport = null
            EmergencySmsDispatcher.clearLastDispatchEvent()
        }
    }

    AnimatedVisibility(
        visible = visibleReport != null,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        visibleReport?.let { report ->
            val count = report.successCount
            val contactsText = if (report.successfulContactNames.isNotEmpty()) {
                report.successfulContactNames.joinToString(", ")
            } else {
                "Guardians"
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A2B) // Deep dark green for high contrast
                ),
                border = BorderStroke(1.5.dp, SafeGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clickable {
                        visibleReport = null
                        EmergencySmsDispatcher.clearLastDispatchEvent()
                    }
                    .testTag("sms_delivery_confirmation_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Green Tick Circle Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SafeGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBangla) {
                                if (count > 0) "SMS সফলভাবে পাঠানো হয়েছে ($count টি কনট্যাক্ট)" else "SMS পাঠানো সম্পন্ন হয়েছে"
                            } else {
                                if (count > 0) "SMS successfully dispatched to $count contact${if (count > 1) "s" else ""}" else "SMS Dispatched"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0) // Mint green highlight
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isBangla) {
                                "প্রাপক: $contactsText • লাইভ GPS লিংক সংযুক্ত"
                            } else {
                                "Sent to: $contactsText • Live GPS attached"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            visibleReport = null
                            EmergencySmsDispatcher.clearLastDispatchEvent()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
