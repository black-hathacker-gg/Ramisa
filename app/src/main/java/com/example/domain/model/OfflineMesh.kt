package com.example.domain.model

enum class MeshNodeType {
  DIRECT_WIFI_AWARE,
  BLUETOOTH_LE_ADVERTISER,
  GUARDIAN_RELAY,
  COMMUNITY_BEACON
}

data class MeshPeerNode(
  val id: String,
  val alias: String,
  val nodeType: MeshNodeType = MeshNodeType.BLUETOOTH_LE_ADVERTISER,
  val signalStrengthDbm: Int, // e.g. -45 dBm (strong), -85 dBm (far)
  val distanceMetersApprox: Int,
  val isTrustedGuardian: Boolean = false,
  val isRelayActive: Boolean = true,
  val lastSeenSecondsAgo: Int = 2
)

data class MeshPacket(
  val packetId: String,
  val senderAlias: String,
  val originalSenderId: String,
  val hopCount: Int = 1,
  val maxHops: Int = 5,
  val latitude: Double = 23.7258,
  val longitude: Double = 90.3976,
  val distressMessage: String,
  val distressMessageBn: String,
  val timestamp: Long = System.currentTimeMillis()
)
