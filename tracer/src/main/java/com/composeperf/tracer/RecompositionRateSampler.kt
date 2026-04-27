package com.composeperf.tracer

/**
 * Periodically samples the RecompositionRegistry and feeds data into the
 * RecompositionRateAnalyzer. Intended to be driven by a coroutine or timer.
 */
class RecompositionRateSampler(
    private val registry: RecompositionRegistry,
    private val analyzer: RecompositionRateAnalyzer
) {

    private val previousCounts = mutableMapOf<String, Int>()

    /**
     * Take a single sample. Call this repeatedly at a consistent interval
     * (e.g. every 200ms) to build up rate data.
     *
     * Only composables whose recomposition count has increased since the last
     * sample are forwarded to the analyzer, avoiding noise from idle components.
     */
    fun sample(nowMs: Long = System.currentTimeMillis()) {
        val snapshot = registry.snapshot()
        snapshot.entries.forEach { (name, currentCount) ->
            val previous = previousCounts[name] ?: 0
            val delta = (currentCount - previous).coerceAtLeast(0)
            if (delta > 0) {
                analyzer.record(name, delta, nowMs)
            }
            previousCounts[name] = currentCount
        }
    }

    /**
     * Returns the names of all composables that have been observed in at least
     * one sample since the last [reset].
     */
    fun trackedComposables(): Set<String> = previousCounts.keys.toSet()

    fun reset() {
        previousCounts.clear()
        analyzer.clear()
    }
}
