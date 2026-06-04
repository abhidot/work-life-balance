package com.worklife.boundary.domain.policy

class BlockingPolicy {
    fun shouldBlock(context: BlockingContext): Boolean {
        if (!context.masterBlockingEnabled) return false
        val entry = context.matchedEntry ?: return false
        if (!entry.enabled) return false
        if (context.scheduleAllowsCall) return false
        return true
    }
}
