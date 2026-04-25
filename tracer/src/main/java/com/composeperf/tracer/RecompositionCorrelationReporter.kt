package com.composeperf.tracer

/**
 * Reports correlated recomposition pairs discovered by [RecompositionCorrelationAnalyzer].
 */
class RecompositionCorrelationReporter(
    private val analyzer: RecompositionCorrelationAnalyzer,
    private val logger: RecompositionLogger = RecompositionLogger(),
    private val minCoOccurrences: Int = 2,
    private val topN: Int = 10
) {

    /**
     * Logs the top correlated composable pairs to the configured logger.
     */
    fun report() {
        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences).take(topN)
        if (pairs.isEmpty()) {
            logger.log("[CorrelationReporter] No significant correlations found.")
            return
        }

        logger.log("[CorrelationReporter] Top correlated recompositions (co-occurrences >= $minCoOccurrences):")
        pairs.forEachIndexed { index, entry ->
            logger.log(
                "  ${index + 1}. \"${entry.first}\" <-> \"${entry.second}\" " +
                    "(co-occurred ${entry.coOccurrenceCount}x)"
            )
        }
    }

    /**
     * Returns a formatted summary string instead of logging it.
     */
    fun buildReport(): String {
        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences).take(topN)
        if (pairs.isEmpty()) return "No significant correlations found."

        return buildString {
            appendLine("Top correlated recompositions (co-occurrences >= $minCoOccurrences):")
            pairs.forEachIndexed { index, entry ->
                appendLine(
                    "  ${index + 1}. \"${entry.first}\" <-> \"${entry.second}\" " +
                        "(co-occurred ${entry.coOccurrenceCount}x)"
                )
            }
        }.trimEnd()
    }
}
