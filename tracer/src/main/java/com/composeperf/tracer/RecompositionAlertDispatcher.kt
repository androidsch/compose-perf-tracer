package com.composeperf.tracer

import android.util.Log

/**
 * Dispatches alerts when recomposition counts exceed configured thresholds.
 *
 * Intended to be called after each recomposition event so that developers
 * receive timely feedback during debug sessions.
 */
class RecompositionAlertDispatcher(
    private val threshold: RecompositionThreshold = RecompositionThreshold.DEFAULT,
    private val tag: String = "RecompositionAlert",
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    // composableKey -> list of timestamps within the current window
    private val windowedCounts: MutableMap<String, ArrayDeque<Long>> = mutableMapOf()

    /**
     * Records a recomposition for [composableKey] and dispatches a log alert
     * if the windowed count exceeds the configured thresholds.
     *
     * @return The [RecompositionSeverity] after recording this event.
     */
    fun record(composableKey: String): RecompositionSeverity {
        val now = clock()
        val timestamps = windowedCounts.getOrPut(composableKey) { ArrayDeque() }

        // Evict entries outside the time window
        val cutoff = now - threshold.windowMs
        while (timestamps.isNotEmpty() && timestamps.first() < cutoff) {
            timestamps.removeFirst()
        }

        timestamps.addLast(now)
        val count = timestamps.size

        val severity = RecompositionSeverity.from(count, threshold)
        dispatchIfNeeded(composableKey, count, severity)
        return severity
    }

    /** Clears all recorded windowed data. */
    fun reset() {
        windowedCounts.clear()
    }

    private fun dispatchIfNeeded(key: String, count: Int, severity: RecompositionSeverity) {
        val message = "[$key] recomposed $count times within ${threshold.windowMs}ms"
        when (severity) {
            RecompositionSeverity.ERROR -> Log.e(tag, "\uD83D\uDD34 EXCESSIVE RECOMPOSITION — $message")
            RecompositionSeverity.WARN  -> Log.w(tag, "\uD83D\uDFE1 HIGH RECOMPOSITION — $message")
            RecompositionSeverity.OK    -> { /* no alert needed */ }
        }
    }
}
