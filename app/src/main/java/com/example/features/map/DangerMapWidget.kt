package com.example.features.map

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary

@Composable
fun DangerMapHomeWidget(
  onOpenFullMap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Sample prominent documented hotspots to display on the mini preview
  val sampleHotspots = listOf(
    Pair("Gabtoli Bus Terminal", "78 Harassment Cases • Critical Danger"),
    Pair("Kuril Flyover Dark Loop", "64 Incidents • Unlit Expressway Ramp"),
    Pair("Sayedabad Alleys", "83 Cases • Severe Night Risk"),
    Pair("Farmgate Overbridge", "115 Harassment Reports • High Risk")
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("card_danger_map_home_widget"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header with Live Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(SafetyRedPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Map,
              contentDescription = null,
              tint = SafetyRedPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "DANGER & HARASSMENT MAP",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "বিপদজনক এলাকা ও ক্রাইম হিটম্যাপ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFFFFEBEE)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(SafetyRedPrimary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "LIVE DATA",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
              color = SafetyRedDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Documented risk zones with highest reported sexual harassment, assault, and unlit corridors in Dhaka. Use safe bypass corridors.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Mini Canvas Heatmap Radar Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF0F172A))
          .clickable(onClick = onOpenFullMap)
          .testTag("preview_danger_map_canvas")
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_mini_radar")
        val radarRadius by infiniteTransition.animateFloat(
          initialValue = 10f,
          targetValue = 45f,
          animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "radar_mini_pulse"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
          initialValue = 0.7f,
          targetValue = 0.0f,
          animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "pulse_mini_alpha"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Grid roads
          drawLine(Color(0xFF1E293B), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 2f)
          drawLine(Color(0xFF1E293B), Offset(w * 0.35f, 0f), Offset(w * 0.35f, h), strokeWidth = 2f)
          drawLine(Color(0xFF1E293B), Offset(w * 0.65f, 0f), Offset(w * 0.65f, h), strokeWidth = 2f)

          // Major arterial roads
          drawLine(Color(0xFF334155), Offset(w * 0.3f, 0f), Offset(w * 0.45f, h), strokeWidth = 4f)
          drawLine(Color(0xFF334155), Offset(0f, h * 0.65f), Offset(w, h * 0.6f), strokeWidth = 4f)

          // Danger heat spots (Gabtoli, Kuril, Sayedabad, Suhrawardy)
          val spots = listOf(
            Offset(w * 0.25f, h * 0.35f), // Gabtoli
            Offset(w * 0.80f, h * 0.22f), // Kuril
            Offset(w * 0.75f, h * 0.80f), // Sayedabad
            Offset(w * 0.50f, h * 0.58f), // Suhrawardy
            Offset(w * 0.45f, h * 0.40f)  // Farmgate
          )

          spots.forEach { spot ->
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEF4444).copy(alpha = 0.6f), Color(0xFFEF4444).copy(alpha = 0.1f), Color.Transparent),
                center = spot,
                radius = 35f
              ),
              center = spot,
              radius = 35f
            )
            drawCircle(Color.White, radius = 5f, center = spot)
            drawCircle(Color(0xFFEF4444), radius = 3.5f, center = spot)
          }

          // User center GPS pin (Dhanmondi axis)
          val userPos = Offset(w * 0.45f, h * 0.52f)
          drawCircle(Color(0xFF00E5FF).copy(alpha = pulseAlpha), radius = radarRadius, center = userPos, style = Stroke(width = 2f))
          drawCircle(Color.White, radius = 6f, center = userPos)
          drawCircle(Color(0xFF00B0FF), radius = 4f, center = userPos)
        }

        // Tap to expand overlay indicator
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF1E293B).copy(alpha = 0.85f),
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.OpenInFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open Interactive Map", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // High Risk Hotspots Quick Summary
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        sampleHotspots.take(2).forEach { (name, info) ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(SafetyRedPrimary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = info, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Button: Full Radar Map
      Button(
        onClick = onOpenFullMap,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("btn_open_danger_map_full")
      ) {
        Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("EXPLORE DANGER ZONES & SAFE BYPASS CORRIDORS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
