package com.composeperf.tracer

/**
 * Represents a burst of recompositions — a cluster of rapid recompositions
 * for a single composable within a short time window.
 */
data class RecompositionBurst(
    val composableName: String,
    val count: Int,
    val windowMs: Long,
    val startTimestamp: Long,
    val endTimestamp: Long
) {
    val durationMs: Long get() = endTimestamp - startTimestamp
    val rate: Double get() = if (durationMs > 0) count.toDouble() / durationMs * 1000.0 else 0.0

    override fun toString(): String =
        "Burst[$composableName]: $count recompositions over ${durationMs}ms (${String.format("%.1f", rate)}/s)"
}
