package com.composeperf.tracer

import android.util.Log

/**
 * Reports the results of [RecompositionFlowAnalyzer] to logcat in a human-readable format.
 */
class RecompositionFlowReporter(
    private val analyzer: RecompositionFlowAnalyzer,
    private val tag: String = "RecompositionFlow",
    private val topN: Int = 10
) {

    /**
     * Logs the top propagating composables and their downstream chains.
     */
    fun report() {
        val propagators = analyzer.topPropagators(topN)
        if (propagators.isEmpty()) {
            Log.d(tag, "No recomposition propagation data recorded.")
            return
        }

        Log.d(tag, "=== Recomposition Flow Report ===")
        Log.d(tag, "Total propagation edges: ${analyzer.edgeCount()}")
        Log.d(tag, "Top $topN propagating composables:")

        propagators.forEachIndexed { index, (name, count) ->
            Log.d(tag, "  ${index + 1}. $name — triggered $count downstream recomposition(s)")
            val chain = analyzer.propagationChain(name)
            if (chain.size > 1) {
                Log.d(tag, "     Chain: ${chain.joinToString(" -> ")}")
            }
        }

        Log.d(tag, "=================================")
    }
}
