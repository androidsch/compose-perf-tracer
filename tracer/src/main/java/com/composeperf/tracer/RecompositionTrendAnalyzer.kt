package com.composeperf.tracer

/**
 * Analyzes recomposition count trends over time windows to detect
 * components that are consistently increasing in recomposition frequency.
 */
class RecompositionTrendAnalyzer(
    private val windowSize: Int = 5
) {

    data class TrendResult(
        val composableName: String,
        val slope: Double,
        val direction: TrendDirection
    )

    enum class TrendDirection { INCREASING, STABLE, DECREASING }

    private val history: MutableMap<String, ArrayDeque<Int>> = mutableMapOf()

    fun record(composableName: String, count: Int) {
        val deque = history.getOrPut(composableName) { ArrayDeque() }
        if (deque.size >= windowSize) deque.removeFirst()
        deque.addLast(count)
    }

    fun analyze(composableName: String): TrendResult {
        val samples = history[composableName] ?: return TrendResult(composableName, 0.0, TrendDirection.STABLE)
        if (samples.size < 2) return TrendResult(composableName, 0.0, TrendDirection.STABLE)
        val slope = computeSlope(samples.toList())
        val direction = when {
            slope > 0.5 -> TrendDirection.INCREASING
            slope < -0.5 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
        return TrendResult(composableName, slope, direction)
    }

    fun analyzeAll(): List<TrendResult> =
        history.keys.map { analyze(it) }

    fun reset() = history.clear()

    private fun computeSlope(values: List<Int>): Double {
        val n = values.size
        val xMean = (n - 1) / 2.0
        val yMean = values.average()
        var numerator = 0.0
        var denominator = 0.0
        values.forEachIndexed { i, y ->
            numerator += (i - xMean) * (y - yMean)
            denominator += (i - xMean) * (i - xMean)
        }
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
}
