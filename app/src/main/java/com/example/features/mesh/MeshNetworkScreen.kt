package com.example.features.mesh

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MeshNodeType
import com.example.domain.model.MeshPacket
import com.example.domain.model.MeshPeerNode
import com.example.ui.theme.SafeGreenDark
import com.example.ui.theme.SafeGreenPrimary
import com.example.ui.theme.SafetyRedDark
import com.example.ui.theme.SafetyRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshNetworkScreen(
  viewModel: MeshNetworkViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  val infiniteTransition = rememberInfiniteTransition(label = "mesh_radar")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (uiState.isScanningPeers) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "mesh_pulse"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Offline Mesh SOS (ইন্টারনেটহীন মেশ)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
      item {
        Spacer(modifier = Modifier.height(4.dp))

        // Hero Mesh Status Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mesh_status"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isMeshBroadcasting) SafeGreenPrimary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant
          ),
          border = if (uiState.isMeshBroadcasting) {
            androidx.compose.foundation.BorderStroke(1.5.dp, SafeGreenPrimary)
          } else null
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isMeshBroadcasting) SafeGreenPrimary else Color.Gray.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = if (uiState.isMeshBroadcasting) Color.White else Color.Gray,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (uiState.isMeshBroadcasting) "PEER-TO-PEER MESH ACTIVE" else "MESH STANDBY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (uiState.isMeshBroadcasting) SafeGreenDark else Color.Gray
                  )
                  Text(
                    text = "Node Alias: ${uiState.localNodeAlias}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = uiState.isMeshBroadcasting,
                onCheckedChange = { viewModel.toggleMeshBroadcasting(it) }
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SafeGreenPrimary.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "Discovered Nodes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${uiState.connectedPeers.size} Nearby", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              }
              Column {
                Text(text = "Relayed Packets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${uiState.totalPacketsRelayed} Relayed", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              }
              Column {
                Text(text = "Coverage Radius", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "~${uiState.broadcastSignalRadiusMeters}m", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }

      // Zero-Internet Mesh Broadcast Trigger Action
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.CellTower, contentDescription = null, tint = SafetyRedPrimary)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Zero-Internet Emergency Broadcast",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Broadcasts multi-hop distress packets to all nearby smartphones without 3G/4G/WiFi.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { viewModel.broadcastOfflineDistressPacket() },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SafetyRedPrimary)
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Broadcast Offline Mesh Packet (Test)")
            }
          }
        }
      }

      // Discovered Mesh Relay Peers
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Active Mesh Relays (${uiState.connectedPeers.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "BLE & Wi-Fi Aware",
            style = MaterialTheme.typography.labelSmall,
            color = SafeGreenDark
          )
        }
      }

      items(uiState.connectedPeers, key = { it.id }) { peer ->
        MeshPeerCard(peer = peer)
      }

      // Relayed Distress Packets History
      if (uiState.relayedPackets.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Relayed Offline Distress Packets",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }

        items(uiState.relayedPackets, key = { it.packetId }) { packet ->
          MeshPacketCard(packet = packet)
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun MeshPeerCard(
  peer: MeshPeerNode,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (peer.isTrustedGuardian) SafeGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (peer.nodeType) {
              MeshNodeType.DIRECT_WIFI_AWARE -> Icons.Default.Wifi
              MeshNodeType.BLUETOOTH_LE_ADVERTISER -> Icons.Default.Bluetooth
              MeshNodeType.GUARDIAN_RELAY -> Icons.Default.Security
              MeshNodeType.COMMUNITY_BEACON -> Icons.Default.CellTower
            },
            contentDescription = null,
            tint = if (peer.isTrustedGuardian) SafeGreenDark else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = peer.alias,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (peer.isTrustedGuardian) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = SafeGreenPrimary.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "GUARDIAN",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                  color = SafeGreenDark,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
          Text(
            text = "Signal: ${peer.signalStrengthDbm} dBm • ~${peer.distanceMetersApprox}m away",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = SafeGreenPrimary.copy(alpha = 0.12f)
      ) {
        Text(
          text = "RELAY ON",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
          color = SafeGreenDark,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
      }
    }
  }
}

@Composable
fun MeshPacketCard(
  packet: MeshPacket,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = SafetyRedPrimary.copy(alpha = 0.08f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyRedPrimary.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SafetyRedPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = packet.packetId,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = SafetyRedDark
          )
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = SafetyRedPrimary.copy(alpha = 0.2f)
        ) {
          Text(
            text = "Hop ${packet.hopCount}/${packet.maxHops}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
            color = SafetyRedDark,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = packet.distressMessage,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = packet.distressMessageBn,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Coordinates: ${packet.latitude}, ${packet.longitude}",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
