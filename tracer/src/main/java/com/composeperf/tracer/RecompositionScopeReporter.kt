package com.composeperf.tracer

import android.util.Log

/**
 * Reports recomposition data collected by [RecompositionScopeTracker].
 * Formats and logs scope-level recomposition summaries.
 */
class RecompositionScopeReporter(
    private val tracker: RecompositionScopeTracker,
    private val tag: String = "RecompositionScope",
    private val logger: (String, String) -> Unit = { t, msg -> Log.d(t, msg) }
) {

    /**
     * Logs all tracked scopes and their recomposition counts.
     */
    fun reportAll() {
        val snapshot = tracker.snapshot()
        if (snapshot.isEmpty()) {
            logger(tag, "No recomposition scopes recorded.")
            return
        }
        val report = buildString {
            appendLine("=== Recomposition Scope Report ===")
            snapshot.entries
                .sortedByDescending { it.value }
                .forEach { (scope, count) ->
                    appendLine("  [$scope] recomposed $count time(s)")
                }
            appendLine("==================================")
        }
        logger(tag, report)
    }

    /**
     * Logs only scopes that exceeded the given [threshold].
     */
    fun reportExceeding(threshold: Int) {
        val offenders = tracker.scopesExceeding(threshold)
        if (offenders.isEmpty()) {
            logger(tag, "No scopes exceeded threshold of $threshold.")
            return
        }
        val report = buildString {
            appendLine("=== Scopes Exceeding Threshold ($threshold) ===")
            offenders.entries
                .sortedByDescending { it.value }
                .forEach { (scope, count) ->
                    appendLine("  [$scope] recomposed $count time(s) — OVER THRESHOLD")
                }
            appendLine("===============================================")
        }
        logger(tag, report)
    }

    /**
     * Returns a plain-text summary string for external use (e.g., export).
     */
    fun summaryText(): String {
        val snapshot = tracker.snapshot()
        return snapshot.entries
            .sortedByDescending { it.value }
            .joinToString(separator = "\n") { (scope, count) -> "$scope=$count" }
    }
}
