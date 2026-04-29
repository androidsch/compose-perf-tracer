package com.composeperf.tracer

import kotlin.math.roundToInt

/**
 * Analyzes recomposition frequency for tracked composables over a sliding time window.
 *
 * Frequency is expressed as recompositions-per-second (rps) based on recorded timestamps.
 */
class RecompositionFrequencyAnalyzer(
    private val windowMillis: Long = 5_000L
) {

    // composable name -> list of event timestamps in millis
    private val eventLog = mutableMapOf<String, ArrayDeque<Long>>()

    /**
     * Records a recomposition event for [composableName] at the given [timestampMillis].
     * Defaults to the current system time if not provided.
     */
    fun record(composableName: String, timestampMillis: Long = System.currentTimeMillis()) {
        val deque = eventLog.getOrPut(composableName) { ArrayDeque() }
        deque.addLast(timestampMillis)
        pruneOldEvents(deque, timestampMillis)
    }

    /**
     * Returns the recomposition frequency (rps) for [composableName] as of [nowMillis].
     * Returns 0.0 if no events are recorded within the window.
     */
    fun frequencyOf(composableName: String, nowMillis: Long = System.currentTimeMillis()): Double {
        val deque = eventLog[composableName] ?: return 0.0
        pruneOldEvents(deque, nowMillis)
        if (deque.isEmpty()) return 0.0
        val windowSeconds = windowMillis / 1_000.0
        return deque.size / windowSeconds
    }

    /**
     * Returns a map of composable name to current frequency (rps) for all tracked composables.
     */
    fun allFrequencies(nowMillis: Long = System.currentTimeMillis()): Map<String, Double> {
        return eventLog.keys.associateWith { frequencyOf(it, nowMillis) }
    }

    /**
     * Returns composables whose frequency exceeds [threshold] rps.
     */
    fun hotComposables(
        threshold: Double,
        nowMillis: Long = System.currentTimeMillis()
    ): Map<String, Double> {
        return allFrequencies(nowMillis).filter { (_, freq) -> freq > threshold }
    }

    /** Clears all recorded events. */
    fun reset() = eventLog.clear()

    private fun pruneOldEvents(deque: ArrayDeque<Long>, nowMillis: Long) {
        val cutoff = nowMillis - windowMillis
        while (deque.isNotEmpty() && deque.first() < cutoff) {
            deque.removeFirst()
        }
    }
}
