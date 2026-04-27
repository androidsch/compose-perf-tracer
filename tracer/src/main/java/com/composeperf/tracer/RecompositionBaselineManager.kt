package com.composeperf.tracer

/**
 * Manages capturing and comparing recomposition baselines.
 *
 * A baseline records the current recomposition counts so that future counts
 * can be compared to detect regressions or unexpected spikes.
 */
class RecompositionBaselineManager(
    private val registry: RecompositionRegistry
) {

    private val baselines = mutableMapOf<String, RecompositionBaseline>()

    /**
     * Captures the current recomposition state as a named baseline.
     * If a baseline with the same label already exists, it is overwritten.
     */
    fun capture(label: String): RecompositionBaseline {
        val snapshot = registry.snapshot()
        val baseline = RecompositionBaseline(
            label = label,
            counts = snapshot.associate { it.key to it.count }
        )
        baselines[label] = baseline
        return baseline
    }

    /**
     * Returns the baseline with the given label, or null if not found.
     */
    fun get(label: String): RecompositionBaseline? = baselines[label]

    /**
     * Compares the current registry state against the named baseline.
     * Returns a [RecompositionBaselineDiff] describing what changed.
     * Returns null if the baseline does not exist.
     */
    fun compare(label: String): RecompositionBaselineDiff? {
        val baseline = baselines[label] ?: return null
        val current = registry.snapshot().associate { it.key to it.count }
        return RecompositionBaselineDiff.compute(baseline, current)
    }

    /**
     * Removes the baseline with the given label.
     */
    fun remove(label: String) {
        baselines.remove(label)
    }

    /**
     * Clears all stored baselines.
     */
    fun clearAll() {
        baselines.clear()
    }

    /**
     * Returns all currently stored baseline labels.
     */
    fun labels(): Set<String> = baselines.keys.toSet()
}
