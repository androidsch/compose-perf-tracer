package com.composeperf.tracer

/**
 * Aggregates recomposition counts across multiple composables,
 * providing summary statistics for a given snapshot map.
 */
class RecompositionAggregator {

    data class AggregationResult(
        val totalRecompositions: Long,
        val uniqueComposables: Int,
        val maxRecompositions: Long,
        val minRecompositions: Long,
        val averageRecompositions: Double,
        val topOffenders: List<Pair<String, Long>>
    )

    fun aggregate(
        counts: Map<String, Long>,
        topN: Int = 5
    ): AggregationResult {
        if (counts.isEmpty()) {
            return AggregationResult(
                totalRecompositions = 0L,
                uniqueComposables = 0,
                maxRecompositions = 0L,
                minRecompositions = 0L,
                averageRecompositions = 0.0,
                topOffenders = emptyList()
            )
        }

        val total = counts.values.fold(0L) { acc, v -> acc + v }
        val max = counts.values.max()
        val min = counts.values.min()
        val average = total.toDouble() / counts.size
        val topOffenders = counts.entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { it.key to it.value }

        return AggregationResult(
            totalRecompositions = total,
            uniqueComposables = counts.size,
            maxRecompositions = max,
            minRecompositions = min,
            averageRecompositions = average,
            topOffenders = topOffenders
        )
    }
}
