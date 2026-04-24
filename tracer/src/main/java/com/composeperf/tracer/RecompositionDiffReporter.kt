package com.composeperf.tracer

/**
 * Reports the diff between two recomposition snapshots in a human-readable format.
 */
class RecompositionDiffReporter(
    private val logger: RecompositionLogger = RecompositionLogger()
) {

    /**
     * Reports all changes (new, removed, increased, decreased) between snapshots.
     */
    fun report(
        previous: RecompositionSnapshot,
        current: RecompositionSnapshot,
        tag: String = "RecompositionDiff"
    ) {
        val diffs = RecompositionDiffCalculator.calculate(previous, current)
        if (diffs.isEmpty()) {
            logger.log(tag, "No recomposition changes detected.")
            return
        }
        logger.log(tag, "Recomposition diff (${diffs.size} changes):")
        diffs.forEach { diff ->
            val label = when {
                diff.isNew -> "[NEW]"
                diff.isRemoved -> "[REMOVED]"
                diff.delta > 0 -> "[+${diff.delta}]"
                else -> "[${diff.delta}]"
            }
            logger.log(tag, "  $label ${diff.composableName} (${diff.previousCount} → ${diff.currentCount})")
        }
    }

    /**
     * Reports only composables whose recomposition count increased.
     */
    fun reportIncreased(
        previous: RecompositionSnapshot,
        current: RecompositionSnapshot,
        tag: String = "RecompositionDiff"
    ) {
        val diffs = RecompositionDiffCalculator.onlyIncreased(previous, current)
        if (diffs.isEmpty()) {
            logger.log(tag, "No recomposition increases detected.")
            return
        }
        logger.log(tag, "Increased recompositions (${diffs.size}):")
        diffs.forEach { diff ->
            logger.log(tag, "  [+${diff.delta}] ${diff.composableName} (total: ${diff.currentCount})")
        }
    }
}
