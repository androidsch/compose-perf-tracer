package com.composeperf.tracer

import kotlin.math.max

/**
 * Analyzes recomposition counts within a sliding time window.
 * Useful for detecting short-lived bursts that span multiple composables.
 */
class RecompositionWindowAnalyzer(
    private val windowSizeMs: Long = 1000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    data class WindowEntry(val composableName: String, val count: Int, val timestampMs: Long)

    data class WindowSummary(
        val composableName: String,
        val totalCount: Int,
        val windowSizeMs: Long,
        val entriesInWindow: Int
    )

    private val entries = mutableListOf<WindowEntry>()

    fun record(composableName: String, count: Int) {
        val now = clock()
        entries.add(WindowEntry(composableName, count, now))
        pruneOldEntries(now)
    }

    fun summarize(): List<WindowSummary> {
        val now = clock()
        pruneOldEntries(now)
        return entries
            .groupBy { it.composableName }
            .map { (name, group) ->
                WindowSummary(
                    composableName = name,
                    totalCount = group.sumOf { it.count },
                    windowSizeMs = windowSizeMs,
                    entriesInWindow = group.size
                )
            }
            .sortedByDescending { it.totalCount }
    }

    fun topN(n: Int): List<WindowSummary> = summarize().take(max(0, n))

    fun reset() = entries.clear()

    private fun pruneOldEntries(now: Long) {
        val cutoff = now - windowSizeMs
        entries.removeAll { it.timestampMs < cutoff }
    }
}
