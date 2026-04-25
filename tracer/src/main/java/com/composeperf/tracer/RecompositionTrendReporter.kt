package com.composeperf.tracer

import android.util.Log

/**
 * Reports trend analysis results for recompositions, highlighting
 * composables with consistently increasing recomposition counts.
 */
class RecompositionTrendReporter(
    private val analyzer: RecompositionTrendAnalyzer,
    private val tag: String = "RecompositionTrend",
    private val onAlert: ((RecompositionTrendAnalyzer.TrendResult) -> Unit)? = null
) {

    fun report() {
        val results = analyzer.analyzeAll()
        if (results.isEmpty()) {
            Log.d(tag, "No trend data available.")
            return
        }
        results.sortedByDescending { it.slope }.forEach { result ->
            val symbol = when (result.direction) {
                RecompositionTrendAnalyzer.TrendDirection.INCREASING -> "▲"
                RecompositionTrendAnalyzer.TrendDirection.DECREASING -> "▼"
                RecompositionTrendAnalyzer.TrendDirection.STABLE -> "─"
            }
            Log.d(
                tag,
                "$symbol ${result.composableName}: slope=%.2f direction=${result.direction}"
                    .format(result.slope)
            )
            if (result.direction == RecompositionTrendAnalyzer.TrendDirection.INCREASING) {
                onAlert?.invoke(result)
            }
        }
    }

    fun reportIncreasing(): List<RecompositionTrendAnalyzer.TrendResult> =
        analyzer.analyzeAll()
            .filter { it.direction == RecompositionTrendAnalyzer.TrendDirection.INCREASING }
            .sortedByDescending { it.slope }
}
