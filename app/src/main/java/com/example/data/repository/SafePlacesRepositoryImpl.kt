package com.example.data.repository

import com.example.domain.model.PlaceType
import com.example.domain.model.SafePlace
import com.example.domain.repository.SafePlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SafePlacesRepositoryImpl : SafePlacesRepository {

  private val placesData = listOf(
    SafePlace(
      id = "SP-01",
      name = "Dhanmondi Model Police Station (ধানমন্ডি থানা)",
      type = PlaceType.POLICE_STATION,
      address = "House 19, Road 8, Dhanmondi, Dhaka",
      area = "Dhanmondi",
      phoneNumber = "+8801713373126",
      latitude = 23.7461,
      longitude = 90.3742,
      distanceKm = 0.8
    ),
    SafePlace(
      id = "SP-02",
      name = "Shahbagh Police Station (শাহবাগ থানা)",
      type = PlaceType.POLICE_STATION,
      address = "Near Dhaka University & BSMMU, Shahbagh, Dhaka",
      area = "Shahbagh / DU",
      phoneNumber = "+8801713373127",
      latitude = 23.7380,
      longitude = 90.3956,
      distanceKm = 1.4
    ),
    SafePlace(
      id = "SP-03",
      name = "Dhaka Medical College Hospital (ঢামেক)",
      type = PlaceType.HOSPITAL,
      address = "Secretariat Road, Bakshi Bazar, Dhaka",
      area = "Old Dhaka / DU",
      phoneNumber = "+880255165088",
      latitude = 23.7259,
      longitude = 90.3976,
      distanceKm = 1.9
    ),
    SafePlace(
      id = "SP-04",
      name = "Square Hospitals Emergency 24/7 (স্কয়ার হাসপাতাল)",
      type = PlaceType.HOSPITAL,
      address = "18/F Bir Uttam Qazi Nuruzzaman Sarak, West Panthapath, Dhaka",
      area = "Panthapath",
      phoneNumber = "+8801713377775",
      latitude = 23.7533,
      longitude = 90.3814,
      distanceKm = 1.1
    ),
    SafePlace(
      id = "SP-05",
      name = "National Trauma & Orthopaedic Hospital (পঙ্গু হাসপাতাল)",
      type = PlaceType.HOSPITAL,
      address = "Sher-e-Bangla Nagar, Mirpur Road, Dhaka",
      area = "Shyamoli",
      phoneNumber = "+88029114075",
      latitude = 23.7709,
      longitude = 90.3687,
      distanceKm = 2.7
    ),
    SafePlace(
      id = "SP-06",
      name = "One-Stop Crisis Centre (OCC) for Women (নারী সহায়তা কেন্দ্র)",
      type = PlaceType.WOMEN_SUPPORT_CENTER,
      address = "Dhaka Medical College Hospital Campus, Dhaka",
      area = "Shahbagh",
      phoneNumber = "109",
      latitude = 23.7262,
      longitude = 90.3980,
      distanceKm = 2.0
    ),
    SafePlace(
      id = "SP-07",
      name = "Mirpur Model Police Station (মিরপুর মডেল থানা)",
      type = PlaceType.POLICE_STATION,
      address = "Section 2, Block D, Mirpur, Dhaka",
      area = "Mirpur",
      phoneNumber = "+8801713373130",
      latitude = 23.8069,
      longitude = 90.3639,
      distanceKm = 3.5
    ),
    SafePlace(
      id = "SP-08",
      name = "Gulshan Police Station (গুলশান থানা)",
      type = PlaceType.POLICE_STATION,
      address = "House 1, Road 71, Gulshan 2, Dhaka",
      area = "Gulshan / Banani",
      phoneNumber = "+8801713373135",
      latitude = 23.7925,
      longitude = 90.4152,
      distanceKm = 4.2
    ),
    SafePlace(
      id = "SP-09",
      name = "Uttara East Police Station (উত্তরা পূর্ব থানা)",
      type = PlaceType.POLICE_STATION,
      address = "Sector 4, Road 18, Uttara, Dhaka",
      area = "Uttara",
      phoneNumber = "+8801713373142",
      latitude = 23.8683,
      longitude = 90.4005,
      distanceKm = 7.1
    ),
    SafePlace(
      id = "SP-10",
      name = "Bangladesh Mahila Samity Safe Haven (মহিলা সমিতি)",
      type = PlaceType.SAFE_ZONE,
      address = "4 Natak Sarani, New Baily Road, Dhaka",
      area = "Baily Road",
      phoneNumber = "+88029334543",
      latitude = 23.7420,
      longitude = 90.4080,
      distanceKm = 2.4
    )
  )

  private val _safePlaces = MutableStateFlow(placesData)
  override val safePlaces: StateFlow<List<SafePlace>> = _safePlaces.asStateFlow()

  override suspend fun searchPlaces(query: String, filterType: PlaceType?): List<SafePlace> {
    val q = query.trim().lowercase()
    return placesData.filter { place ->
      val matchesQuery = q.isEmpty() ||
          place.name.lowercase().contains(q) ||
          place.area.lowercase().contains(q) ||
          place.address.lowercase().contains(q)
      val matchesType = filterType == null || place.type == filterType
      matchesQuery && matchesType
    }
  }

  override suspend fun getNearbyPlaces(latitude: Double, longitude: Double): List<SafePlace> {
    return placesData.map { place ->
      val dist = calculateHaversineKm(latitude, longitude, place.latitude, place.longitude)
      place.copy(distanceKm = String.format("%.1f", dist).toDoubleOrNull() ?: dist)
    }.sortedBy { it.distanceKm }
  }

  private fun calculateHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
