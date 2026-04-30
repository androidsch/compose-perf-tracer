package com.composeperf.tracer

import android.util.Log

/**
 * Reports the contents of a [RecompositionTimeline] in a human-readable format.
 */
class RecompositionTimelineReporter(
    private val timeline: RecompositionTimeline,
    private val tag: String = "RecompositionTimeline",
    private val logger: (String, String) -> Unit = { t, msg -> Log.d(t, msg) }
) {

    /**
     * Logs all timeline entries ordered chronologically.
     */
    fun reportAll() {
        val entries = timeline.entries
        if (entries.isEmpty()) {
            logger(tag, "Timeline is empty — no recompositions recorded.")
            return
        }
        logger(tag, "=== Recomposition Timeline (${entries.size} entries) ===")
        entries.forEach { entry ->
            logger(tag, "[${entry.timestampMs}ms] ${entry.composableName} — count: ${entry.count}")
        }
    }

    /**
     * Logs only entries for a specific composable.
     */
    fun reportFor(composableName: String) {
        val entries = timeline.entriesFor(composableName)
        if (entries.isEmpty()) {
            logger(tag, "No timeline entries found for '$composableName'.")
            return
        }
        logger(tag, "=== Timeline for '$composableName' (${entries.size} entries) ===")
        entries.forEach { entry ->
            logger(tag, "[${entry.timestampMs}ms] count: ${entry.count}")
        }
    }

    /**
     * Logs entries within a specific time window.
     */
    fun reportInRange(fromMs: Long, toMs: Long) {
        val entries = timeline.entriesInRange(fromMs, toMs)
        if (entries.isEmpty()) {
            logger(tag, "No timeline entries in range [$fromMs, $toMs].")
            return
        }
        logger(tag, "=== Timeline [$fromMs–$toMs] (${entries.size} entries) ===")
        entries.forEach { entry ->
            logger(tag, "[${entry.timestampMs}ms] ${entry.composableName} — count: ${entry.count}")
        }
    }
}
