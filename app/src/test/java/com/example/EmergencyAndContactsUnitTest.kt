package com.example

import com.example.data.repository.EmergencyRepositoryImpl
import com.example.domain.model.EmergencyStatus
import com.example.domain.model.GeoLocation
import com.example.domain.model.TriggerType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EmergencyAndContactsUnitTest {
    @Test
    fun openingAnEmergencyDoesNotInventDeliveryOrLocation() = runTest {
        val repository = EmergencyRepositoryImpl()
        val event = repository.triggerSos(null, TriggerType.SOS_BUTTON, "", false).getOrThrow()
        assertFalse(repository.currentLocation.value.isAvailable)
        assertEquals(EmergencyStatus.SOS_ACTIVE, repository.activeEmergency.value?.status)
        val beforeReceipt = repository.dispatchAlerts(event.id, 3, GeoLocation()).getOrThrow()
        assertEquals(0, beforeReceipt.smsSentCount)
        assertEquals(0, beforeReceipt.pushDeliveredCount)
        assertFalse(beforeReceipt.nationalHotlineDialed)
        assertEquals("", beforeReceipt.locationSharedUrl)
        repository.recordSmsConfirmation("other-event", 1)
        assertEquals(EmergencyStatus.SOS_ACTIVE, repository.activeEmergency.value?.status)
        repository.recordSmsConfirmation(event.id, 1)
        assertEquals(EmergencyStatus.CONTACTS_NOTIFIED, repository.activeEmergency.value?.status)
    }

    @Test
    fun resolveRequiresTheConfiguredPin() = runTest {
        val repository = EmergencyRepositoryImpl()
        val event = repository.triggerSos(null, TriggerType.SOS_BUTTON, "", false).getOrThrow()
        assertTrue(repository.resolveEmergency(event.id, "0000", "2468").isFailure)
        assertTrue(repository.resolveEmergency(event.id, "1234", "2468").isFailure)
        assertTrue(repository.resolveEmergency(event.id, "", "").isFailure)
        assertTrue(repository.resolveEmergency(event.id, "2468", "2468").isSuccess)
        assertNull(repository.activeEmergency.value)
        assertEquals(EmergencyStatus.RESOLVED, repository.emergencyHistory.value.single().status)
    }

    @Test
    fun localTestDoesNotClaimDeliveryAndHistoryStartsEmpty() = runTest {
        val repository = EmergencyRepositoryImpl()
        assertTrue(repository.emergencyHistory.value.isEmpty())
        val test = repository.triggerTestSos().getOrThrow()
        assertEquals(TriggerType.TEST_SOS, test.triggerType)
        assertNull(repository.activeEmergency.value)
        assertEquals(test, repository.emergencyHistory.value.single())
        assertTrue(repository.clearHistory())
        assertTrue(repository.emergencyHistory.value.isEmpty())
    }
}
