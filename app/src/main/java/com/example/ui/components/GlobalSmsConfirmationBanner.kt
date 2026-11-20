package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.sms.EmergencySmsDispatcher

/** Receipt-backed status. Submission, modem acceptance and recipient delivery are distinct. */
@Composable
fun GlobalSmsConfirmationBanner(modifier: Modifier = Modifier, isBangla: Boolean = false) {
    val report by EmergencySmsDispatcher.lastDispatchEvent.collectAsStateWithLifecycle()
    AnimatedVisibility(visible = report != null, modifier = modifier) {
        report?.let { status ->
            val confirmed = status.deliveredCount > 0 && status.failedContactNames.isEmpty()
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (confirmed) Color(0xFF173D2C) else Color(0xFF263448)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("sms_delivery_confirmation_banner")
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(status.statusSummary, color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isBangla) "পাঠানো নিশ্চিত: ${status.successCount}/${status.totalAttempted} • পৌঁছানো নিশ্চিত: ${status.deliveredCount} • অপেক্ষায়: ${status.pendingCount}"
                            else "Sent confirmed: ${status.successCount}/${status.totalAttempted} • Delivery confirmed: ${status.deliveredCount} • Awaiting status: ${status.pendingCount}",
                            color = Color.White, style = MaterialTheme.typography.bodySmall
                        )
                        val location = status.locationUsed
                        Text(
                            when {
                                location?.isAvailable != true -> "No location attached."
                                location.locationAgeMillis == null -> "Saved location; fix age is unknown."
                                location.locationAgeMillis!! > 30_000L -> "Cached location (${location.locationAgeMillis!! / 1000}s old)."
                                location.isApproximate -> "Approximate device location attached."
                                else -> "Recent device location attached."
                            },
                            color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall
                        )
                        if (status.failedContactNames.isNotEmpty()) Text(
                            status.failedContactNames.joinToString("; "), color = Color(0xFFFFCCBC),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { EmergencySmsDispatcher.clearLastDispatchEvent() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss SMS status", tint = Color.White)
                    }
                }
            }
        }
    }
}
