package com.composeperf.tracer

import android.util.Log

/**
 * Reports annotation-driven policy violations and summaries to the log.
 */
class RecompositionAnnotationReporter(
    private val processor: RecompositionAnnotationProcessor,
    private val registry: RecompositionRegistry,
    private val tag: String = "RecompositionAnnotationReporter"
) {

    /**
     * Logs a summary of all tracked composables, highlighting those that
     * violate [StableComposable] constraints or are marked [HighFrequencyComposable].
     */
    fun report() {
        val snapshot = registry.snapshot()
        if (snapshot.isEmpty()) {
            Log.d(tag, "No recomposition data to report.")
            return
        }

        Log.d(tag, "=== Recomposition Annotation Report ===")
        snapshot.forEach { (label, count) ->
            val stableLimit = processor.getStableLimit(label)
            val isHighFreq = processor.isHighFrequency(label)

            val annotation = when {
                isHighFreq -> "[HIGH-FREQ]"
                stableLimit != null && count > stableLimit -> "[STABLE-VIOLATION]"
                stableLimit != null -> "[STABLE-OK]"
                else -> ""
            }

            Log.d(tag, "  $annotation $label -> $count recompositions")
        }
        Log.d(tag, "=======================================")
    }
}
