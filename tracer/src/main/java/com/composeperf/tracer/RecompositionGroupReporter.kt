package com.composeperf.tracer

/**
 * Reports recomposition group analysis results using a provided logger.
 */
class RecompositionGroupReporter(
    private val analyzer: RecompositionGroupAnalyzer = RecompositionGroupAnalyzer(),
    private val logger: RecompositionLogger = RecompositionLogger()
) {

    /**
     * Logs a summary of recomposition counts grouped by key.
     *
     * @param snapshots List of recomposition snapshots to report on.
     * @param topN Number of top groups to highlight.
     */
    fun report(snapshots: List<RecompositionSnapshot>, topN: Int = 5) {
        if (snapshots.isEmpty()) {
            logger.log("[GroupReporter] No recomposition data available.")
            return
        }

        val summary = analyzer.summarize(snapshots)
        logger.log("[GroupReporter] Recomposition summary by group (${summary.size} groups):")
        summary
            .entries
            .sortedByDescending { it.value }
            .forEach { (group, count) ->
                logger.log("  Group='$group' totalRecompositions=$count")
            }

        val top = analyzer.topGroups(snapshots, topN)
        logger.log("[GroupReporter] Top $topN groups:")
        top.forEachIndexed { index, (group, count) ->
            logger.log("  #${index + 1} '$group' -> $count recompositions")
        }
    }
}
