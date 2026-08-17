package com.example.features.mesh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.MeshNodeType
import com.example.domain.model.MeshPacket
import com.example.domain.model.MeshPeerNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class MeshNetworkUiState(
  val isMeshBroadcasting: Boolean = true,
  val isScanningPeers: Boolean = true,
  val connectedPeers: List<MeshPeerNode> = emptyList(),
  val relayedPackets: List<MeshPacket> = emptyList(),
  val totalPacketsRelayed: Int = 18,
  val isRelayForwardingEnabled: Boolean = true,
  val broadcastSignalRadiusMeters: Int = 120,
  val localNodeAlias: String = "RAMISA-DU-Node-71"
)

class MeshNetworkViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(MeshNetworkUiState())
  val uiState: StateFlow<MeshNetworkUiState> = _uiState.asStateFlow()

  private var peerSimulationJob: Job? = null

  init {
    loadInitialPeers()
    startPeerDiscoverySimulation()
  }

  private fun loadInitialPeers() {
    val initial = listOf(
      MeshPeerNode(
        id = "peer_du_curzon_01",
        alias = "Curzon Guard Relay (Node #12)",
        nodeType = MeshNodeType.DIRECT_WIFI_AWARE,
        signalStrengthDbm = -52,
        distanceMetersApprox = 15,
        isTrustedGuardian = true,
        isRelayActive = true
      ),
      MeshPeerNode(
        id = "peer_du_tsc_04",
        alias = "TSC Community Mesh Beacon #4",
        nodeType = MeshNodeType.BLUETOOTH_LE_ADVERTISER,
        signalStrengthDbm = -68,
        distanceMetersApprox = 42,
        isTrustedGuardian = false,
        isRelayActive = true
      ),
      MeshPeerNode(
        id = "peer_rokeya_hall_09",
        alias = "Rokeya Hall Gate Beacon (Node #9)",
        nodeType = MeshNodeType.GUARDIAN_RELAY,
        signalStrengthDbm = -74,
        distanceMetersApprox = 65,
        isTrustedGuardian = true,
        isRelayActive = true
      ),
      MeshPeerNode(
        id = "peer_shahbagh_sub_02",
        alias = "Shahbagh Metro Mesh Node #2",
        nodeType = MeshNodeType.DIRECT_WIFI_AWARE,
        signalStrengthDbm = -81,
        distanceMetersApprox = 95,
        isTrustedGuardian = false,
        isRelayActive = true
      )
    )

    val samplePackets = listOf(
      MeshPacket(
        packetId = "PKT-RMS-881",
        senderAlias = "User #9012 (Curzon Gate)",
        originalSenderId = "USR-9012",
        hopCount = 2,
        maxHops = 5,
        latitude = 23.7272,
        longitude = 90.4012,
        distressMessage = "Emergency beacon relayed via 2 mesh hops (No SIM / Offline)",
        distressMessageBn = "২টি অফলাইন মেশ হপের মাধ্যমে জরুরি সংকেত ফরোয়ার্ড করা হয়েছে।"
      )
    )

    _uiState.update {
      it.copy(
        connectedPeers = initial,
        relayedPackets = samplePackets
      )
    }
  }

  private fun startPeerDiscoverySimulation() {
    peerSimulationJob?.cancel()
    peerSimulationJob = viewModelScope.launch {
      while (isActive) {
        delay(4000)
        if (_uiState.value.isScanningPeers) {
          _uiState.update { state ->
            val updated = state.connectedPeers.map { peer ->
              val jitter = Random.nextInt(-4, 5)
              val newSignal = (peer.signalStrengthDbm + jitter).coerceIn(-90, -40)
              val newDist = when {
                newSignal > -60 -> Random.nextInt(10, 25)
                newSignal > -75 -> Random.nextInt(30, 65)
                else -> Random.nextInt(70, 115)
              }
              peer.copy(
                signalStrengthDbm = newSignal,
                distanceMetersApprox = newDist,
                lastSeenSecondsAgo = Random.nextInt(1, 4)
              )
            }
            state.copy(connectedPeers = updated)
          }
        }
      }
    }
  }

  fun toggleMeshBroadcasting(enabled: Boolean) {
    _uiState.update { it.copy(isMeshBroadcasting = enabled) }
  }

  fun toggleScanning(enabled: Boolean) {
    _uiState.update { it.copy(isScanningPeers = enabled) }
  }

  fun toggleRelayForwarding(enabled: Boolean) {
    _uiState.update { it.copy(isRelayForwardingEnabled = enabled) }
  }

  fun broadcastOfflineDistressPacket() {
    val newPacket = MeshPacket(
      packetId = "PKT-RMS-" + UUID.randomUUID().toString().take(4).uppercase(),
      senderAlias = _uiState.value.localNodeAlias,
      originalSenderId = "LOCAL-DEVICE",
      hopCount = 1,
      maxHops = 5,
      latitude = 23.7258,
      longitude = 90.3976,
      distressMessage = "OFFLINE DISTRESS BEACON: Zero-Internet Bluetooth & Wi-Fi Aware Flood Active!",
      distressMessageBn = "অফলাইন বিপদ সংকেত: ইন্টারনেট ও মোবাইল নেটওয়ার্ক ছাড়াই মেশ রিলের মাধ্যমে পাঠানো হয়েছে!"
    )

    _uiState.update {
      it.copy(
        relayedPackets = listOf(newPacket) + it.relayedPackets,
        totalPacketsRelayed = it.totalPacketsRelayed + 1
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    peerSimulationJob?.cancel()
  }
}
