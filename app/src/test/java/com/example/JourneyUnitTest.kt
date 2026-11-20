package com.example

import com.example.data.repository.JourneyRepositoryImpl
import com.example.domain.model.GeoLocation
import com.example.domain.model.JourneyStatus
import com.example.domain.model.TravelMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyUnitTest {
  @Test
  fun journeyTracksLocationUntilItIsCompleted() = runTest {
    val repository = JourneyRepositoryImpl()
    val first = repository.startJourney("Dhanmondi 27", "TSC", 30, TravelMode.RICKSHAW).getOrThrow()
    val location = GeoLocation(23.735, 90.395)
    assertTrue(repository.updateLocation(location).getOrThrow())
    assertEquals(location, repository.activeJourney.value?.lastLocation)

    assertFalse(repository.completeJourney("wrong-id").getOrThrow())
    assertEquals(first.id, repository.activeJourney.value?.id)
    assertTrue(repository.completeJourney(first.id).getOrThrow())
    assertNull(repository.activeJourney.value)
    assertEquals(JourneyStatus.SAFE_ARRIVED, repository.journeyHistory.value.first().status)
  }

  @Test
  fun requestingHelpLeavesJourneyAvailableDuringSosReview() = runTest {
    val repository = JourneyRepositoryImpl()
    val journey = repository.startJourney("Dhanmondi 27", "TSC", 30, TravelMode.RICKSHAW).getOrThrow()
    repository.respondToSafetyCheck(safe = false)
    assertTrue(repository.isDeviationDetected.value)
    assertEquals(journey.id, repository.activeJourney.value?.id)
    assertTrue(repository.cancelJourney(journey.id).getOrThrow())
    assertEquals(JourneyStatus.CANCELLED, repository.journeyHistory.value.first().status)
  }
}
