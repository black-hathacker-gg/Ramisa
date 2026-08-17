package com.example.domain.repository

import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.flow.StateFlow

interface DangerZonesRepository {
  val dangerZones: StateFlow<List<DangerZone>>
  suspend fun getNearbyDangerZones(lat: Double, lng: Double, radiusKm: Double = 15.0): List<DangerZone>
  suspend fun searchDangerZones(query: String, riskFilter: RiskLevel?, categoryFilter: DangerCategory?): List<DangerZone>
  suspend fun reportNewUnsafeLocation(name: String, area: String, reason: String, lat: Double, lng: Double): Boolean
}
