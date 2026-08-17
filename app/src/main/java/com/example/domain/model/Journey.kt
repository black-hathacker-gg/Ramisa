package com.example.domain.model

enum class TravelMode {
  WALKING,
  RICKSHAW,
  CNG_AUTO,
  BUS,
  CAR,
  TRAIN
}

enum class JourneyStatus {
  NOT_STARTED,
  IN_PROGRESS,
  SAFE_ARRIVED,
  ALERT_TRIGGERED,
  CANCELLED
}

data class Journey(
  val id: String,
  val startLocation: String,
  val destination: String,
  val expectedArrivalMinutes: Int,
  val travelMode: TravelMode,
  val notes: String = "",
  val status: JourneyStatus = JourneyStatus.NOT_STARTED,
  val startTimeMillis: Long = System.currentTimeMillis()
)
