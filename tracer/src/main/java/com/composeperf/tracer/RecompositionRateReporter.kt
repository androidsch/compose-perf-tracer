package com.composeperf.tracer

import android.util.Log

/**
 * Reports recomposition rate analysis results.
 * Flags composables whose recompositions-per-second exceed a given threshold.
 */
class RecompositionRateReporter(
    private val analyzer: RecompositionRateAnalyzer,
    private val rpsThreshold: Double = 5.0,
    private val tag: String = "RecompositionRate"
) {

    fun report(nowMs: Long = System.currentTimeMillis()) {
        val entries = analyzer.analyze(nowMs)
        if (entries.isEmpty()) {
            Log.d(tag, "No recomposition rate data available.")
            return
        }
        Log.d(tag, "--- Recomposition Rate Report ---")
        entries.forEach { entry ->
            val marker = if (entry.recompositionsPerSecond >= rpsThreshold) " ⚠️" else ""
            Log.d(
                tag,
                "${entry.composableName}: ${entry.recompositionsPerSecond} rps " +
                    "(${entry.count} in ${entry.windowMs}ms)$marker"
            )
        }
        Log.d(tag, "---------------------------------")
    }

    fun hotComposables(nowMs: Long = System.currentTimeMillis()): List<RecompositionRateAnalyzer.RateEntry> {
        return analyzer.analyze(nowMs).filter { it.recompositionsPerSecond >= rpsThreshold }
    }
}
