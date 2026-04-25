package com.composeperf.tracer

import android.util.Log

/**
 * Reports the results of a [RecompositionAggregator.AggregationResult] to logcat.
 */
class RecompositionAggregatorReporter(
    private val tag: String = "RecompositionAggregator"
) {

    fun report(result: RecompositionAggregator.AggregationResult) {
        if (result.uniqueComposables == 0) {
            Log.d(tag, "No recomposition data to report.")
            return
        }

        Log.d(tag, "=== Recomposition Aggregation Report ===")
        Log.d(tag, "  Unique composables : ${result.uniqueComposables}")
        Log.d(tag, "  Total recompositions: ${result.totalRecompositions}")
        Log.d(tag, "  Max recompositions  : ${result.maxRecompositions}")
        Log.d(tag, "  Min recompositions  : ${result.minRecompositions}")
        Log.d(tag, "  Avg recompositions  : ${"%,.2f".format(result.averageRecompositions)}")

        if (result.topOffenders.isNotEmpty()) {
            Log.d(tag, "  Top offenders:")
            result.topOffenders.forEachIndexed { index, (name, count) ->
                Log.d(tag, "    ${index + 1}. $name -> $count")
            }
        }
        Log.d(tag, "========================================")
    }
}
