package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.schedule.GlobalSchedule
import com.worklife.boundary.domain.schedule.ScheduleEvaluator
import java.time.ZonedDateTime

/**
 * Composes matching, schedule evaluation, and blocking policy for one incoming call.
 */
class CallDecisionEngine(
    private val matchResolver: CallMatchResolver = CallMatchResolver(),
    private val scheduleEvaluator: ScheduleEvaluator = ScheduleEvaluator(),
    private val blockingPolicy: BlockingPolicy = BlockingPolicy(),
) {
    data class Input(
        val incomingNumber: String,
        val now: ZonedDateTime,
        val masterBlockingEnabled: Boolean,
        val globalSchedule: GlobalSchedule,
        val entries: List<BlocklistEntry>,
    )

    data class Decision(
        val shouldBlock: Boolean,
        val matchedEntry: BlocklistEntry?,
    )

    fun evaluate(input: Input): Decision {
        val matched = matchResolver.resolve(input.incomingNumber, input.entries)
        val scheduleAllows = matched?.let { entry ->
            scheduleEvaluator.isAllowed(input.now, input.globalSchedule, entry.scheduleOverrides)
        } ?: true

        val shouldBlock = blockingPolicy.shouldBlock(
            BlockingContext(
                masterBlockingEnabled = input.masterBlockingEnabled,
                matchedEntry = matched,
                scheduleAllowsCall = scheduleAllows,
            ),
        )
        return Decision(shouldBlock = shouldBlock, matchedEntry = matched)
    }
}
