package com.composeperf.tracer

import kotlin.math.roundToInt

/**
 * Analyzes recomposition rates over a sliding time window.
 * Computes recompositions-per-second for each tracked composable.
 */
class RecompositionRateAnalyzer(
    private val windowMs: Long = 1000L
) {

    data class RateEntry(
        val composableName: String,
        val count: Int,
        val windowMs: Long,
        val recompositionsPerSecond: Double
    )

    private data class TimestampedCount(
        val composableName: String,
        val count: Int,
        val recordedAtMs: Long
    )

    private val history = mutableListOf<TimestampedCount>()

    fun record(composableName: String, count: Int, nowMs: Long = System.currentTimeMillis()) {
        require(count >= 0) { "count must be non-negative, got $count" }
        history.removeAll { nowMs - it.recordedAtMs > windowMs }
        history.add(TimestampedCount(composableName, count, nowMs))
    }

    fun analyze(nowMs: Long = System.currentTimeMillis()): List<RateEntry> {
        history.removeAll { nowMs - it.recordedAtMs > windowMs }
        return history
            .groupBy { it.composableName }
            .map { (name, entries) ->
                val totalCount = entries.sumOf { it.count }
                val rps = (totalCount / (windowMs / 1000.0) * 100).roundToInt() / 100.0
                RateEntry(
                    composableName = name,
                    count = totalCount,
                    windowMs = windowMs,
                    recompositionsPerSecond = rps
                )
            }
            .sortedByDescending { it.recompositionsPerSecond }
    }

    /**
     * Returns the [RateEntry] for a specific composable, or null if it has no
     * recorded events within the current window.
     */
    fun analyzeFor(
        composableName: String,
        nowMs: Long = System.currentTimeMillis()
    ): RateEntry? = analyze(nowMs).find { it.composableName == composableName }

    fun clear() {
        history.clear()
    }
}
