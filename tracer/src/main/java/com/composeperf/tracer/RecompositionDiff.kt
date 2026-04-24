package com.composeperf.tracer

/**
 * Represents the difference in recomposition counts between two snapshots.
 */
data class RecompositionDiff(
    val composableName: String,
    val previousCount: Int,
    val currentCount: Int
) {
    val delta: Int get() = currentCount - previousCount
    val isNew: Boolean get() = previousCount == 0
    val isRemoved: Boolean get() = currentCount == 0

    override fun toString(): String =
        "RecompositionDiff(name=$composableName, prev=$previousCount, curr=$currentCount, delta=$delta)"
}

/**
 * Computes the diff between two recomposition snapshots.
 */
object RecompositionDiffCalculator {

    fun calculate(
        previous: RecompositionSnapshot,
        current: RecompositionSnapshot
    ): List<RecompositionDiff> {
        val allKeys = previous.counts.keys + current.counts.keys
        return allKeys
            .distinct()
            .map { name ->
                RecompositionDiff(
                    composableName = name,
                    previousCount = previous.counts[name] ?: 0,
                    currentCount = current.counts[name] ?: 0
                )
            }
            .filter { it.delta != 0 }
            .sortedByDescending { it.delta }
    }

    fun onlyIncreased(
        previous: RecompositionSnapshot,
        current: RecompositionSnapshot
    ): List<RecompositionDiff> =
        calculate(previous, current).filter { it.delta > 0 }
}
