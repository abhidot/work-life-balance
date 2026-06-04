package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.schedule.EntryScheduleOverrides

data class BlocklistEntry(
    val id: Long,
    val displayName: String,
    val contactId: String?,
    val enabled: Boolean,
    val numbers: List<String>,
    val scheduleOverrides: EntryScheduleOverrides,
)

data class BlockingContext(
    val masterBlockingEnabled: Boolean,
    val matchedEntry: BlocklistEntry?,
    val scheduleAllowsCall: Boolean,
)
