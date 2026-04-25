package com.composeperf.tracer

/**
 * Periodically samples recomposition counts from the registry and feeds
 * them into the [RecompositionTrendAnalyzer] to build trend windows.
 */
class RecompositionTrendSampler(
    private val registry: RecompositionRegistry,
    private val analyzer: RecompositionTrendAnalyzer
) {
    private var sampleCount: Int = 0

    /**
     * Takes a snapshot of current counts and records them in the analyzer.
     * Should be called at regular intervals (e.g., every second or every frame batch).
     */
    fun sample() {
        val snapshot = registry.snapshot()
        snapshot.forEach { (name, count) ->
            analyzer.record(name, count)
        }
        sampleCount++
    }

    fun getSampleCount(): Int = sampleCount

    fun reset() {
        sampleCount = 0
        analyzer.reset()
    }
}
