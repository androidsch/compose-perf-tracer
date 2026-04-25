package com.composeperf.tracer

/**
 * Detects outlier composables whose recomposition counts deviate significantly
 * from the mean using a configurable Z-score threshold.
 */
class RecompositionOutlierDetector(
    private val zScoreThreshold: Double = 2.0
) {

    data class OutlierResult(
        val composableName: String,
        val count: Int,
        val zScore: Double
    )

    /**
     * Analyzes the given recomposition counts and returns composables
     * whose counts are statistical outliers.
     *
     * @param counts map of composable name to recomposition count
     * @return list of [OutlierResult] sorted by z-score descending
     */
    fun detect(counts: Map<String, Int>): List<OutlierResult> {
        if (counts.size < 2) return emptyList()

        val values = counts.values.map { it.toDouble() }
        val mean = values.average()
        val stdDev = stdDev(values, mean)

        if (stdDev == 0.0) return emptyList()

        return counts
            .mapNotNull { (name, count) ->
                val z = (count - mean) / stdDev
                if (z >= zScoreThreshold) OutlierResult(name, count, z) else null
            }
            .sortedByDescending { it.zScore }
    }

    private fun stdDev(values: List<Double>, mean: Double): Double {
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        return Math.sqrt(variance)
    }
}
