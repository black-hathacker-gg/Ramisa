package com.example

import com.example.core.sms.*
import com.example.domain.model.GeoLocation
import org.junit.Assert.*
import org.junit.Test

class SmsReceiptReducerTest {
    private fun event() = SmsLedgerEvent("event", "SOS", listOf(
        SmsRecipientLedger("a", "Contact A", "", listOf(SmsPartReceipt(0), SmsPartReceipt(1))),
        SmsRecipientLedger("b", "Contact B", "", listOf(SmsPartReceipt(0)))
    ), createdAt = 100L)

    @Test fun submissionDoesNotClaimSentOrDelivered() {
        val state = SmsReceiptReducer.submitted(event(), "a")
        assertTrue(state.recipients[0].pending)
        assertFalse(state.recipients[0].sentConfirmed)
        assertFalse(state.recipients[0].deliveredConfirmed)
    }

    @Test fun multipartPartialFailureIsNotSuccessfulContact() {
        var state = SmsReceiptReducer.sent(event(), "a", 0, true, null)
        state = SmsReceiptReducer.sent(state, "a", 1, false, "radio off")
        assertFalse(state.recipients[0].sentConfirmed)
        assertTrue(state.recipients[0].failed)
        assertFalse(state.recipients[1].sentConfirmed)
    }

    @Test fun duplicateCallbacksDoNotIncreaseCountsOrUndoSuccess() {
        var state = SmsReceiptReducer.sent(event(), "a", 0, true, null)
        val once = state
        state = SmsReceiptReducer.sent(state, "a", 0, true, null)
        assertEquals(once, state)
        state = SmsReceiptReducer.sent(state, "a", 0, false, "stale callback")
        assertEquals(once, state)
        assertEquals(1, state.recipients[0].parts.count { it.sendState == SmsSendState.SENT })
    }

    @Test fun deliveryBeforeSentCallbackIsNotDowngraded() {
        var state = SmsReceiptReducer.delivery(event(), "b", 0, true, false, null)
        state = SmsReceiptReducer.sent(state, "b", 0, false, "late failure")
        state = SmsReceiptReducer.delivery(state, "b", 0, false, true, "duplicate failure")
        assertTrue(state.recipients[1].sentConfirmed)
        assertTrue(state.recipients[1].deliveredConfirmed)
        assertFalse(state.recipients[1].failed)
    }

    @Test fun everyPartNeedsDeliveryReceipt() {
        var state = SmsReceiptReducer.sent(event(), "a", 0, true, null)
        state = SmsReceiptReducer.sent(state, "a", 1, true, null)
        state = SmsReceiptReducer.delivery(state, "a", 0, true, false, null)
        assertTrue(state.recipients[0].sentConfirmed)
        assertFalse(state.recipients[0].deliveredConfirmed)
        state = SmsReceiptReducer.delivery(state, "a", 1, true, false, null)
        assertTrue(state.recipients[0].deliveredConfirmed)
    }

    @Test fun processRestartMakesPendingUnknownWithoutInventingSuccess() {
        var state = SmsReceiptReducer.submitted(event(), "a")
        state = SmsReceiptReducer.recoverAfterRestart(state)
        assertTrue(state.recipients[0].unknown)
        assertFalse(state.recipients[0].pending)
        assertFalse(state.recipients[0].sentConfirmed)
        state = SmsReceiptReducer.sent(state, "a", 0, true, null)
        assertEquals(SmsSendState.SENT, state.recipients[0].parts[0].sendState)
        assertFalse(state.recipients[0].sentConfirmed)
    }

    @Test fun unknownRecipientAndPartCannotMutateEvent() {
        val initial = event()
        assertEquals(initial, SmsReceiptReducer.sent(initial, "missing", 0, true, null))
        assertEquals(initial, SmsReceiptReducer.delivery(initial, "a", 99, true, false, null))
    }

    @Test fun noSimNoPermissionNoContactsFailPreflight() {
        assertNotNull(SmsPreflight.blockedReason(0, true, true))
        assertNotNull(SmsPreflight.blockedReason(2, false, true))
        assertNotNull(SmsPreflight.blockedReason(2, true, false))
        assertNotNull(SmsPreflight.blockedReason(2, true, true, false))
        assertNull(SmsPreflight.blockedReason(2, true, true, true))
    }

    @Test fun locationUnavailableHasNoInventedCoordinatesAndZeroCoordinatesAreValid() {
        assertFalse(GeoLocation().isAvailable)
        assertTrue(GeoLocation(0.0, 0.0).isAvailable)
        val fix = GeoLocation(1.0, 2.0, timestamp = 10_000L, elapsedRealtimeNanos = 1_000_000_000L)
        assertEquals(2_000L, fix.ageMillis(3_000_000_000L, 100_000L))
    }
}
