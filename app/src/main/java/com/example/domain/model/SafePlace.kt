package com.example.domain.model

enum class PlaceType {
  POLICE_STATION,
  HOSPITAL,
  WOMEN_SUPPORT_CENTER,
  SAFE_ZONE
}

data class SafePlace(
  val id: String,
  val name: String,
  val type: PlaceType,
  val address: String,
  val area: String,
  val phoneNumber: String,
  val latitude: Double,
  val longitude: Double,
  val isOpen24x7: Boolean = true,
  val distanceKm: Double = 1.2
)
