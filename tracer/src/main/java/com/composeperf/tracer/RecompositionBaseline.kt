package com.composeperf.tracer

/**
 * Represents a captured baseline of recomposition counts at a specific point in time.
 * Used to compare current counts against a known-good state.
 */
data class RecompositionBaseline(
    val label: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val counts: Map<String, Int>
) {

    /**
     * Returns the count for a specific composable key, or 0 if not present.
     */
    fun countFor(key: String): Int = counts.getOrDefault(key, 0)

    /**
     * Returns the total recomposition count across all composables in this baseline.
     */
    val total: Int get() = counts.values.sum()

    /**
     * Returns the number of distinct composables tracked in this baseline.
     */
    val size: Int get() = counts.size

    override fun toString(): String =
        "RecompositionBaseline(label='$label', capturedAt=$capturedAt, total=$total, size=$size)"
}
