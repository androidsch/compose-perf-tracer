package com.composeperf.tracer

/**
 * Groups recomposition snapshots by a key derived from composable names,
 * allowing analysis of related composables (e.g., by screen or feature prefix).
 */
class RecompositionGroupAnalyzer(
    private val groupKeyExtractor: (String) -> String = ::defaultGroupKey
) {

    /**
     * Groups the given snapshots by their derived group key.
     *
     * @param snapshots List of recomposition snapshots to group.
     * @return A map of group key to list of snapshots in that group.
     */
    fun group(snapshots: List<RecompositionSnapshot>): Map<String, List<RecompositionSnapshot>> {
        return snapshots.groupBy { groupKeyExtractor(it.composableName) }
    }

    /**
     * Returns a summary of total recomposition counts per group.
     *
     * @param snapshots List of recomposition snapshots to summarize.
     * @return A map of group key to total recomposition count.
     */
    fun summarize(snapshots: List<RecompositionSnapshot>): Map<String, Int> {
        return group(snapshots).mapValues { (_, groupSnapshots) ->
            groupSnapshots.sumOf { it.count }
        }
    }

    /**
     * Returns the top N groups by total recomposition count.
     *
     * @param snapshots List of recomposition snapshots.
     * @param topN Number of top groups to return.
     * @return List of pairs (groupKey, totalCount) sorted descending.
     */
    fun topGroups(snapshots: List<RecompositionSnapshot>, topN: Int = 5): List<Pair<String, Int>> {
        return summarize(snapshots)
            .entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { it.key to it.value }
    }
}

/**
 * Default group key extractor: uses the first segment of a dot-separated composable name,
 * or the full name if no dot is present.
 */
private fun defaultGroupKey(composableName: String): String {
    return composableName.substringBefore('.')
}
