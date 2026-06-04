package com.worklife.boundary.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import com.worklife.boundary.BoundaryApplication
import com.worklife.boundary.data.BoundaryMappers
import com.worklife.boundary.domain.policy.CallDecisionEngine
import com.worklife.boundary.domain.policy.CallDecisionEngine.Input
import kotlinx.coroutines.runBlocking
import java.time.ZonedDateTime

class BoundaryCallScreeningService : CallScreeningService() {

    private val decisionEngine = CallDecisionEngine()

    override fun onScreenCall(callDetails: Call.Details) {
        val response = runBlocking {
            try {
                val app = application as BoundaryApplication
                val repository = app.repository
                val settings = repository.getSettings()
                val entries = repository.loadBlocklistForScreening()
                val global = BoundaryMappers.toGlobalSchedule(settings)
                val incoming = callDetails.handle?.schemeSpecificPart ?: return@runBlocking allow()

                val decision = decisionEngine.evaluate(
                    Input(
                        incomingNumber = incoming,
                        now = ZonedDateTime.now(),
                        masterBlockingEnabled = settings.masterBlockingEnabled,
                        globalSchedule = global,
                        entries = entries,
                    ),
                )

                if (decision.shouldBlock) {
                    repository.recordBlockEvent(
                        normalizedNumber = incoming,
                        entryId = decision.matchedEntry?.id,
                        displayName = decision.matchedEntry?.displayName,
                    )
                    blockAsUnreachable()
                } else {
                    allow()
                }
            } catch (_: Exception) {
                allow()
            }
        }
        respondToCall(callDetails, response)
    }

    private fun allow(): CallScreeningService.CallResponse =
        CallScreeningService.CallResponse.Builder().build()

    /**
     * Block without simulating a manual user reject.
     * disallowCall stops the call from connecting locally; rejectCall=false avoids
     * "declined" semantics where possible (carrier may still play busy/unavailable).
     */
    private fun blockAsUnreachable(): CallScreeningService.CallResponse =
        CallScreeningService.CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(false)
            .setSkipCallLog(true)
            .setSkipNotification(true)
            .build()
}
