package com.composeperf.tracer

/**
 * Analyzes correlations between composables that tend to recompose together,
 * helping identify shared state or unstable parameters causing cascading recompositions.
 */
class RecompositionCorrelationAnalyzer(
    private val windowSize: Int = 50
) {

    // Tracks co-occurrence counts: key = sorted pair of composable names
    private val coOccurrenceMap = mutableMapOf<Pair<String, String>, Int>()
    private val recompositionWindows = mutableListOf<Set<String>>()

    /**
     * Records a batch of composables that recomposed within the same frame/window.
     */
    fun recordWindow(composables: Set<String>) {
        if (composables.size < 2) return

        recompositionWindows.add(composables)
        if (recompositionWindows.size > windowSize) {
            recompositionWindows.removeAt(0)
        }

        val list = composables.sorted()
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                val pair = Pair(list[i], list[j])
                coOccurrenceMap[pair] = (coOccurrenceMap[pair] ?: 0) + 1
            }
        }
    }

    /**
     * Returns pairs of composables sorted by co-occurrence count descending.
     * Only includes pairs that co-occurred at least [minCoOccurrences] times.
     */
    fun getCorrelatedPairs(minCoOccurrences: Int = 2): List<CorrelationEntry> {
        return coOccurrenceMap
            .filter { it.value >= minCoOccurrences }
            .map { (pair, count) -> CorrelationEntry(pair.first, pair.second, count) }
            .sortedByDescending { it.coOccurrenceCount }
    }

    /**
     * Returns the strongest correlation partner for a given composable name.
     */
    fun topCorrelationFor(composableName: String): CorrelationEntry? {
        return getCorrelatedPairs(minCoOccurrences = 1)
            .filter { it.first == composableName || it.second == composableName }
            .maxByOrNull { it.coOccurrenceCount }
    }

    fun reset() {
        coOccurrenceMap.clear()
        recompositionWindows.clear()
    }
}

data class CorrelationEntry(
    val first: String,
    val second: String,
    val coOccurrenceCount: Int
)
