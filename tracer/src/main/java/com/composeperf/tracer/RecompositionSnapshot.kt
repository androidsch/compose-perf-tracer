package com.composeperf.tracer

import androidx.annotation.VisibleForTesting

/**
 * Immutable snapshot of recomposition data captured at a point in time.
 */
data class RecompositionSnapshot(
    val composableName: String,
    val recompositionCount: Int,
    val capturedAtMs: Long = System.currentTimeMillis()
) {
    val isAboveThreshold: Boolean
        get() = recompositionCount > 0
}

/**
 * Aggregated report built from a collection of [RecompositionSnapshot] entries.
 */
data class RecompositionReport(
    val snapshots: List<RecompositionSnapshot>,
    val generatedAtMs: Long = System.currentTimeMillis()
) {
    val totalRecompositions: Int
        get() = snapshots.sumOf { it.recompositionCount }

    val hotspots: List<RecompositionSnapshot>
        get() = snapshots.sortedByDescending { it.recompositionCount }

    val isEmpty: Boolean
        get() = snapshots.isEmpty()

    @VisibleForTesting
    fun topN(n: Int): List<RecompositionSnapshot> = hotspots.take(n)
}
