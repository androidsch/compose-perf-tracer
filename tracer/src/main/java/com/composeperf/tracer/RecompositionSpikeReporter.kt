package com.composeperf.tracer

import android.util.Log

/**
 * Reports detected recomposition spikes via logcat.
 * Intended for use in debug builds to surface sudden bursts of recompositions.
 */
class RecompositionSpikeReporter(
    private val detector: RecompositionSpikeDetector,
    private val tag: String = "RecompositionSpikeReporter",
    private val logger: (String, String) -> Unit = { t, msg -> Log.w(t, msg) }
) {

    /**
     * Evaluates the given snapshot entries for spikes and logs any that are found.
     *
     * @param snapshot map of composable name to current recomposition count
     * @return list of spike events detected in this report cycle
     */
    fun report(snapshot: Map<String, Int>): List<RecompositionSpikeDetector.SpikeEvent> {
        val spikes = mutableListOf<RecompositionSpikeDetector.SpikeEvent>()

        for ((name, count) in snapshot) {
            val spike = detector.record(name, count)
            if (spike != null) {
                spikes += spike
                logger(
                    tag,
                    "Recomposition spike detected in '$name': " +
                        "count=$count, avg=%.2f, multiplier=%.2fx".format(
                            spike.rollingAverage,
                            spike.multiplier
                        )
                )
            }
        }

        return spikes
    }
}
