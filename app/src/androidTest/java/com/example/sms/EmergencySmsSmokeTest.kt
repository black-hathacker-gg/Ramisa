package com.example.sms

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.sms.EmergencySmsDispatcher
import com.example.domain.model.GeoLocation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class EmergencySmsSmokeTest {
    @Test
    fun freshTestInstallBlocksSosWithoutContactsAndDoesNotClaimDelivery() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val eventId = "smoke-${UUID.randomUUID()}"
        val report = EmergencySmsDispatcher.dispatchEmergencySms(context, "Emulator self-test", GeoLocation(), eventId)
        assertEquals(eventId, report.eventId)
        assertEquals(0, report.totalAttempted)
        assertEquals(0, report.successCount)
        assertEquals(0, report.deliveredCount)
        assertEquals(0, report.pendingCount)
        assertTrue(report.statusSummary, report.statusSummary.contains("contact", ignoreCase = true))
        assertTrue(report.isTerminal)
        assertNull(report.locationUsed)
        val duplicate = EmergencySmsDispatcher.dispatchEmergencySms(context, "Duplicate", GeoLocation(), eventId)
        assertEquals(report, duplicate)
    }
}
