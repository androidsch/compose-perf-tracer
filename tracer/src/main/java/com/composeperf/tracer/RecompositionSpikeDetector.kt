package com.composeperf.tracer

/**
 * Detects sudden spikes in recomposition counts for tracked composables.
 * A spike is defined as a recomposition count that exceeds the rolling average
 * by a configurable multiplier within a given window of samples.
 */
class RecompositionSpikeDetector(
    private val windowSize: Int = 5,
    private val spikeMultiplier: Double = 3.0
) {

    private val history: MutableMap<String, ArrayDeque<Int>> = mutableMapOf()

    data class SpikeEvent(
        val composableName: String,
        val currentCount: Int,
        val rollingAverage: Double,
        val multiplier: Double
    )

    /**
     * Records a new recomposition count sample for the given composable.
     * Returns a [SpikeEvent] if a spike is detected, or null otherwise.
     */
    fun record(composableName: String, count: Int): SpikeEvent? {
        val window = history.getOrPut(composableName) { ArrayDeque() }

        if (window.size >= windowSize) {
            window.removeFirst()
        }

        val average = if (window.isEmpty()) 0.0 else window.average()
        window.addLast(count)

        if (average > 0.0 && count >= average * spikeMultiplier) {
            return SpikeEvent(
                composableName = composableName,
                currentCount = count,
                rollingAverage = average,
                multiplier = count / average
            )
        }

        return null
    }

    /**
     * Resets all recorded history.
     */
    fun reset() {
        history.clear()
    }

    /**
     * Returns the current rolling window for a given composable, or empty list if none.
     */
    fun getWindow(composableName: String): List<Int> {
        return history[composableName]?.toList() ?: emptyList()
    }
}
