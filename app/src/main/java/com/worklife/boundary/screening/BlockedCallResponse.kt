package com.worklife.boundary.screening

import android.telecom.CallScreeningService

data class BlockedCallFlags(
    val disallowCall: Boolean,
    val rejectCall: Boolean,
    val skipCallLog: Boolean,
    val skipNotification: Boolean,
)

fun blockedCallFlags(sendToVoicemail: Boolean): BlockedCallFlags =
    BlockedCallFlags(
        disallowCall = true,
        rejectCall = sendToVoicemail,
        skipCallLog = !sendToVoicemail,
        skipNotification = true,
    )

object BlockedCallResponse {
    fun build(sendToVoicemail: Boolean): CallScreeningService.CallResponse {
        val flags = blockedCallFlags(sendToVoicemail)
        return CallScreeningService.CallResponse.Builder()
            .setDisallowCall(flags.disallowCall)
            .setRejectCall(flags.rejectCall)
            .setSkipCallLog(flags.skipCallLog)
            .setSkipNotification(flags.skipNotification)
            .build()
    }
}
