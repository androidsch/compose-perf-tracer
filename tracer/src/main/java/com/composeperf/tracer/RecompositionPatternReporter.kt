package com.composeperf.tracer

import android.util.Log

/**
 * Reports detected recomposition patterns using the configured logger.
 */
class RecompositionPatternReporter(
    private val detector: RecompositionPatternDetector,
    private val tag: String = "RecompositionPatternReporter",
    private val logger: (String) -> Unit = { message -> Log.d("ComposePerfTracer", message) }
) {

    /**
     * Runs pattern detection and logs any discovered patterns.
     * Returns the list of detected patterns for further processing.
     */
    fun report(): List<RecompositionPatternDetector.Pattern> {
        val patterns = detector.detectPatterns()
        if (patterns.isEmpty()) {
            logger("[$tag] No recurring recomposition patterns detected.")
        } else {
            logger("[$tag] Detected ${patterns.size} recomposition pattern(s):")
            patterns.forEachIndexed { index, pattern ->
                logger("[$tag]   #${index + 1} ${pattern.description}")
            }
        }
        return patterns
    }

    /**
     * Convenience method that records a batch and immediately checks for patterns.
     */
    fun recordAndReport(composables: List<String>): List<RecompositionPatternDetector.Pattern> {
        detector.recordBatch(composables)
        return report()
    }
}
