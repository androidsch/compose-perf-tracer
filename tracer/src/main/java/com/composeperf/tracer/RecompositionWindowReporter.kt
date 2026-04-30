package com.composeperf.tracer

import android.util.Log

/**
 * Reports the results of [RecompositionWindowAnalyzer] to logcat.
 */
class RecompositionWindowReporter(
    private val analyzer: RecompositionWindowAnalyzer,
    private val tag: String = "RecompositionWindow",
    private val topN: Int = 10,
    private val logger: (String, String) -> Unit = { t, msg -> Log.d(t, msg) }
) {

    fun report() {
        val summaries = analyzer.topN(topN)
        if (summaries.isEmpty()) {
            logger(tag, "No recomposition data in current window.")
            return
        }
        val sb = StringBuilder()
        sb.appendLine("=== Recomposition Window Report (top $topN) ===")
        summaries.forEachIndexed { index, summary ->
            sb.appendLine(
                "${index + 1}. ${summary.composableName} " +
                    "| count=${summary.totalCount} " +
                    "| entries=${summary.entriesInWindow} " +
                    "| window=${summary.windowSizeMs}ms"
            )
        }
        logger(tag, sb.toString().trimEnd())
    }

    fun reportAndReset() {
        report()
        analyzer.reset()
    }
}
