package com.composeperf.tracer

/**
 * An immutable snapshot of recomposition counts at a point in time.
 */
data class RecompositionSnapshot(
    val timestampMs: Long = System.currentTimeMillis(),
    val counts: Map<String, Int> = emptyMap()
) {
    val totalRecompositions: Int get() = counts.values.sum()
    val composableCount: Int get() = counts.size

    fun topN(n: Int): List<Pair<String, Int>> =
        counts.entries
            .sortedByDescending { it.value }
            .take(n)
            .map { it.key to it.value }

    companion object {
        val EMPTY = RecompositionSnapshot(counts = emptyMap())
    }
}
