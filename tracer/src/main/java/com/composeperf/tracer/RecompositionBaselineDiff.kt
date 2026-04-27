package com.composeperf.tracer

/**
 * Represents the difference between a captured baseline and a current snapshot.
 */
data class RecompositionBaselineDiff(
    val baselineLabel: String,
    val baselineCapturedAt: Long,
    /** Keys whose counts increased relative to the baseline. */
    val increased: Map<String, Int>,
    /** Keys that are new (not present in the baseline). */
    val added: Map<String, Int>,
    /** Keys present in the baseline but no longer tracked. */
    val removed: Set<String>
) {

    val hasChanges: Boolean
        get() = increased.isNotEmpty() || added.isNotEmpty() || removed.isNotEmpty()

    val totalIncrease: Int
        get() = increased.values.sum() + added.values.sum()

    override fun toString(): String = buildString {
        appendLine("BaselineDiff(label='$baselineLabel')")
        if (increased.isNotEmpty()) {
            appendLine("  Increased (${increased.size}):")
            increased.forEach { (k, v) -> appendLine("    $k: +$v") }
        }
        if (added.isNotEmpty()) {
            appendLine("  Added (${added.size}):")
            added.forEach { (k, v) -> appendLine("    $k: $v") }
        }
        if (removed.isNotEmpty()) {
            appendLine("  Removed (${removed.size}): ${removed.joinToString()}")
        }
        if (!hasChanges) appendLine("  No changes detected.")
    }

    companion object {
        fun compute(
            baseline: RecompositionBaseline,
            current: Map<String, Int>
        ): RecompositionBaselineDiff {
            val increased = mutableMapOf<String, Int>()
            val added = mutableMapOf<String, Int>()
            val removed = mutableSetOf<String>()

            for ((key, currentCount) in current) {
                val baselineCount = baseline.countFor(key)
                if (baselineCount == 0 && !baseline.counts.containsKey(key)) {
                    added[key] = currentCount
                } else if (currentCount > baselineCount) {
                    increased[key] = currentCount - baselineCount
                }
            }

            for (key in baseline.counts.keys) {
                if (!current.containsKey(key)) removed.add(key)
            }

            return RecompositionBaselineDiff(
                baselineLabel = baseline.label,
                baselineCapturedAt = baseline.capturedAt,
                increased = increased,
                added = added,
                removed = removed
            )
        }
    }
}
