package com.example.data.repository

import com.example.domain.model.DangerCategory
import com.example.domain.model.DangerZone
import com.example.domain.model.RiskLevel
import com.example.domain.repository.DangerZonesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DangerZonesRepositoryImpl : DangerZonesRepository {

  private val initialZones = listOf(
    DangerZone(
      id = "DZ-01",
      name = "Gabtoli Inter-District Terminal & Embankment",
      areaName = "Gabtoli, Mirpur",
      district = "Dhaka",
      riskLevel = RiskLevel.CRITICAL_DANGER,
      category = DangerCategory.SEXUAL_HARASSMENT_HOTSPOT,
      latitude = 23.7845,
      longitude = 90.3440,
      radiusMeters = 800,
      reportedIncidentsSummary = "Multiple severe sexual harassment & assault incidents reported along the dark embankment and bus parking lanes.",
      incidentStats = "78 Verified Reports • 34 Police Cases",
      peakVulnerableHours = "9:30 PM - 5:30 AM",
      nearestPoliceStation = "Darus Salam Police Station",
      policeContact = "+8801713373133",
      safetyAdvisory = "Never walk alone along the embankment or unlit bus bays after 9 PM. Use main lighted highway with verified ride-shares.",
      safeAlternativeRoute = "Use Mirpur Road main lighted boulevard or Gabtoli Bridge highway with active police picket.",
      isLightingPoor = true,
      hasCctvCoverage = false,
      userReportsCount = 214
    ),
    DangerZone(
      id = "DZ-02",
      name = "Kuril Flyover Dark Loop & 300 Feet Entryway",
      areaName = "Kuril / Purbachal Link Road",
      district = "Dhaka",
      riskLevel = RiskLevel.CRITICAL_DANGER,
      category = DangerCategory.ISOLATED_DARK_CORRIDOR,
      latitude = 23.8190,
      longitude = 90.4208,
      radiusMeters = 700,
      reportedIncidentsSummary = "High vulnerability zone for women commuters in CNGs and rickshaws due to unlit flyover loops and isolated expressway ramps.",
      incidentStats = "64 Harassment Incidents • 22 Snatching/Assaults",
      peakVulnerableHours = "10:00 PM - 4:00 AM",
      nearestPoliceStation = "Khilkhet Police Station",
      policeContact = "+8801713373140",
      safetyAdvisory = "Avoid taking isolated three-wheelers without active GPS tracking through the dark loop ramp.",
      safeAlternativeRoute = "Take the main Airport Road (Pragati Sarani) lighted lane; avoid unlit side loops.",
      isLightingPoor = true,
      hasCctvCoverage = false,
      userReportsCount = 189
    ),
    DangerZone(
      id = "DZ-03",
      name = "Suhrawardy Udyan & Ramna Secluded Perimeters",
      areaName = "Shahbagh / Dhaka University",
      district = "Dhaka",
      riskLevel = RiskLevel.HIGH_RISK,
      category = DangerCategory.UNMONITORED_PARK_ALLEY,
      latitude = 23.7335,
      longitude = 90.3980,
      radiusMeters = 600,
      reportedIncidentsSummary = "Repeated harassment, stalking, and eve-teasing cases in secluded unmonitored garden corners and rear wall alleyways.",
      incidentStats = "92 Harassment Reports • 45 Eve-teasing Logs",
      peakVulnerableHours = "7:00 PM - 11:00 PM",
      nearestPoliceStation = "Shahbagh Police Station",
      policeContact = "+8801713373127",
      safetyAdvisory = "Stick strictly to the illuminated main TSC/Shahbagh sidewalk; avoid entering unlit inner park pathways after dusk.",
      safeAlternativeRoute = "Walk along TSC - Shahbagh Main Avenue where university security guards and streetlights are active.",
      isLightingPoor = true,
      hasCctvCoverage = true,
      userReportsCount = 310
    ),
    DangerZone(
      id = "DZ-04",
      name = "Sayedabad Terminal & Dholairpar Underpass",
      areaName = "Sayedabad / Jatrabari",
      district = "Dhaka",
      riskLevel = RiskLevel.CRITICAL_DANGER,
      category = DangerCategory.HIGH_CRIME_TRANSIT_HUB,
      latitude = 23.7120,
      longitude = 90.4310,
      radiusMeters = 900,
      reportedIncidentsSummary = "Frequent harassment, gang intimidation, and assault reports targeting female transit passengers in unpatrolled terminal alleys.",
      incidentStats = "83 Reported Incidents • 29 Police Interventions",
      peakVulnerableHours = "8:30 PM - 5:00 AM",
      nearestPoliceStation = "Jatrabari Police Station",
      policeContact = "+8801713373145",
      safetyAdvisory = "Do not board informal human haulers or unverified CNGs inside the terminal back alleys.",
      safeAlternativeRoute = "Use Mayor Hanif Flyover designated passenger ramps with active transport police booths.",
      isLightingPoor = true,
      hasCctvCoverage = false,
      userReportsCount = 275
    ),
    DangerZone(
      id = "DZ-05",
      name = "Hatirjheel Madhubagh & Ulan Dark Walkway Bridges",
      areaName = "Hatirjheel / Madhubagh",
      district = "Dhaka",
      riskLevel = RiskLevel.HIGH_RISK,
      category = DangerCategory.ISOLATED_DARK_CORRIDOR,
      latitude = 23.7650,
      longitude = 90.4110,
      radiusMeters = 550,
      reportedIncidentsSummary = "Isolated bridge corners and unlit lakeside walkways where harassment and stalking incidents have been documented at night.",
      incidentStats = "51 Harassment Cases • 18 Stalking Reports",
      peakVulnerableHours = "10:30 PM - 4:30 AM",
      nearestPoliceStation = "Hatirjheel Police Station",
      policeContact = "+8801713373155",
      safetyAdvisory = "Avoid lone pedestrian walks along Madhubagh rear bridge after 10 PM. Use water taxi or main Hatirjheel circle road.",
      safeAlternativeRoute = "Use Hatirjheel Main Boulevard with 24/7 security patrol and open lighting.",
      isLightingPoor = true,
      hasCctvCoverage = true,
      userReportsCount = 167
    ),
    DangerZone(
      id = "DZ-06",
      name = "Farmgate Foot Overbridge & Park Alleys",
      areaName = "Farmgate / Tejgaon",
      district = "Dhaka",
      riskLevel = RiskLevel.HIGH_RISK,
      category = DangerCategory.SEXUAL_HARASSMENT_HOTSPOT,
      latitude = 23.7570,
      longitude = 90.3890,
      radiusMeters = 400,
      reportedIncidentsSummary = "Extreme congestion during rush hour leading to eve-teasing and harassment; isolated dark overbridge staircases at night.",
      incidentStats = "115 Harassment Reports • 42 Incident Logs",
      peakVulnerableHours = "6:30 PM - 10:30 PM",
      nearestPoliceStation = "Tejgaon Police Station",
      policeContact = "+8801713373132",
      safetyAdvisory = "Cross with fellow female commuter crowds; avoid lingering on dark stair landings.",
      safeAlternativeRoute = "Use street-level zebra crossing under traffic police guidance when overbridge is isolated.",
      isLightingPoor = false,
      hasCctvCoverage = true,
      userReportsCount = 420
    ),
    DangerZone(
      id = "DZ-07",
      name = "Mirpur Section 10 & 11 Dark Bihari Camp Alleyways",
      areaName = "Mirpur, Section 10/11",
      district = "Dhaka",
      riskLevel = RiskLevel.HIGH_RISK,
      category = DangerCategory.UNMONITORED_PARK_ALLEY,
      latitude = 23.8070,
      longitude = 90.3685,
      radiusMeters = 650,
      reportedIncidentsSummary = "Narrow, dimly lit unmonitored passages with frequent reports of verbal and physical harassment of women.",
      incidentStats = "68 Harassment Reports • 31 Local Complaints",
      peakVulnerableHours = "8:00 PM - 2:00 AM",
      nearestPoliceStation = "Pallabi Police Station",
      policeContact = "+8801713373138",
      safetyAdvisory = "Avoid taking unlit shortcuts through interior camp lanes; always stay on broad main avenues.",
      safeAlternativeRoute = "Mirpur 10 Metro Rail Avenue (Begum Rokeya Sarani) with well-lit metro pillars and CCTV.",
      isLightingPoor = true,
      hasCctvCoverage = false,
      userReportsCount = 205
    ),
    DangerZone(
      id = "DZ-08",
      name = "Badda Link Road & Notun Bazar Rear Waterways",
      areaName = "Badda / Gulshan Link",
      district = "Dhaka",
      riskLevel = RiskLevel.MODERATE_CAUTION,
      category = DangerCategory.ISOLATED_DARK_CORRIDOR,
      latitude = 23.7810,
      longitude = 90.4260,
      radiusMeters = 500,
      reportedIncidentsSummary = "Dimly lit connecting alleys between Badda and Gulshan canal where stalking incidents have been logged.",
      incidentStats = "38 Harassment Reports • 14 Safety Warnings",
      peakVulnerableHours = "9:00 PM - 1:00 AM",
      nearestPoliceStation = "Badda Police Station",
      policeContact = "+8801713373139",
      safetyAdvisory = "Do not commute alone on foot across the canal footbridge late at night.",
      safeAlternativeRoute = "Gulshan 1 - Badda Main Link Road with regular police patrol cars.",
      isLightingPoor = true,
      hasCctvCoverage = false,
      userReportsCount = 145
    )
  )

  private val _dangerZones = MutableStateFlow(initialZones)
  override val dangerZones: StateFlow<List<DangerZone>> = _dangerZones.asStateFlow()

  override suspend fun getNearbyDangerZones(lat: Double, lng: Double, radiusKm: Double): List<DangerZone> {
    return _dangerZones.value.sortedBy { zone ->
      calculateDistanceKm(lat, lng, zone.latitude, zone.longitude)
    }
  }

  override suspend fun searchDangerZones(
    query: String,
    riskFilter: RiskLevel?,
    categoryFilter: DangerCategory?
  ): List<DangerZone> {
    val q = query.trim().lowercase()
    return _dangerZones.value.filter { zone ->
      val matchesQuery = q.isEmpty() ||
          zone.name.lowercase().contains(q) ||
          zone.areaName.lowercase().contains(q) ||
          zone.reportedIncidentsSummary.lowercase().contains(q)
      val matchesRisk = riskFilter == null || zone.riskLevel == riskFilter
      val matchesCategory = categoryFilter == null || zone.category == categoryFilter
      matchesQuery && matchesRisk && matchesCategory
    }
  }

  override suspend fun reportNewUnsafeLocation(
    name: String,
    area: String,
    reason: String,
    lat: Double,
    lng: Double
  ): Boolean {
    val newZone = DangerZone(
      id = "DZ-${System.currentTimeMillis() % 1000}",
      name = name,
      areaName = area,
      riskLevel = RiskLevel.HIGH_RISK,
      category = DangerCategory.SEXUAL_HARASSMENT_HOTSPOT,
      latitude = lat,
      longitude = lng,
      radiusMeters = 500,
      reportedIncidentsSummary = reason,
      incidentStats = "1 Community Report • Under Investigation",
      peakVulnerableHours = "Evening / Night",
      nearestPoliceStation = "DMP Quick Response Unit",
      policeContact = "999",
      safetyAdvisory = "Community reported high risk area. Proceed with vigilance.",
      safeAlternativeRoute = "Seek main illuminated avenue.",
      userReportsCount = 1
    )
    _dangerZones.value = listOf(newZone) + _dangerZones.value
    return true
  }

  private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }
}
