package com.composeperf.tracer

import android.util.Log

/**
 * Reports detected recomposition bursts via Android logcat.
 */
class RecompositionBurstReporter(
    private val tag: String = "RecompositionBurst",
    private val logger: (String, String) -> Unit = { t, msg -> Log.w(t, msg) }
) {
    /**
     * Reports a single [burst] if it is non-null.
     * Returns true if a burst was reported.
     */
    fun report(burst: RecompositionBurst?): Boolean {
        if (burst == null) return false
        logger(tag, burst.toString())
        return true
    }

    /**
     * Reports all [bursts] in the list.
     * Returns the number of bursts reported.
     */
    fun reportAll(bursts: List<RecompositionBurst>): Int {
        if (bursts.isEmpty()) return 0
        logger(tag, "=== Recomposition Burst Report (${bursts.size} active bursts) ===")
        bursts.forEach { burst ->
            logger(tag, "  ${burst}")
        }
        return bursts.size
    }
}
