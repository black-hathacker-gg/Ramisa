package com.example.domain.repository

import com.example.domain.model.PlaceType
import com.example.domain.model.SafePlace
import kotlinx.coroutines.flow.StateFlow

interface SafePlacesRepository {
  val safePlaces: StateFlow<List<SafePlace>>
  suspend fun searchPlaces(query: String, filterType: PlaceType? = null): List<SafePlace>
  suspend fun getNearbyPlaces(latitude: Double, longitude: Double): List<SafePlace>
}
