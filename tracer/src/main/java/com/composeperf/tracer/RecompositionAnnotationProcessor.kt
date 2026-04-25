package com.composeperf.tracer

import android.util.Log

/**
 * Processes annotation metadata for tracked composables and enforces
 * annotation-driven policies (e.g., [StableComposable] violation alerts).
 */
class RecompositionAnnotationProcessor(
    private val alertDispatcher: RecompositionAlertDispatcher,
    private val registry: RecompositionRegistry
) {

    private val highFrequencyLabels = mutableSetOf<String>()
    private val stableLimits = mutableMapOf<String, Int>()

    /**
     * Registers annotation metadata for a composable identified by [label].
     */
    fun register(
        label: String,
        trackAnnotation: TrackRecomposition? = null,
        highFreqAnnotation: HighFrequencyComposable? = null,
        stableAnnotation: StableComposable? = null
    ) {
        if (highFreqAnnotation != null) {
            highFrequencyLabels.add(label)
            Log.d(TAG, "[$label] marked as high-frequency: ${highFreqAnnotation.reason}")
        }
        if (stableAnnotation != null) {
            stableLimits[label] = stableAnnotation.maxAllowed
            Log.d(TAG, "[$label] marked as stable, max recompositions: ${stableAnnotation.maxAllowed}")
        }
    }

    /**
     * Evaluates current recomposition counts against annotation policies.
     * Should be called periodically or after each session.
     */
    fun evaluate() {
        val snapshot = registry.snapshot()
        snapshot.forEach { (label, count) ->
            val limit = stableLimits[label] ?: return@forEach
            if (count > limit) {
                alertDispatcher.dispatch(
                    label = label,
                    count = count,
                    message = "StableComposable [$label] exceeded max recompositions: $count > $limit"
                )
            }
        }
    }

    fun isHighFrequency(label: String): Boolean = label in highFrequencyLabels

    fun getStableLimit(label: String): Int? = stableLimits[label]

    private companion object {
        private const val TAG = "RecompositionAnnotationProcessor"
    }
}
