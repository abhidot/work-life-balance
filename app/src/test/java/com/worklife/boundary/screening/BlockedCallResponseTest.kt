package com.worklife.boundary.screening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedCallResponseTest {

    @Test
    fun voicemail_mode_rejects_call_for_carrier_routing() {
        val flags = blockedCallFlags(sendToVoicemail = true)
        assertTrue(flags.disallowCall)
        assertTrue(flags.rejectCall)
        assertFalse(flags.skipCallLog)
    }

    @Test
    fun unreachable_mode_blocks_without_user_reject() {
        val flags = blockedCallFlags(sendToVoicemail = false)
        assertTrue(flags.disallowCall)
        assertFalse(flags.rejectCall)
        assertTrue(flags.skipCallLog)
    }
}
