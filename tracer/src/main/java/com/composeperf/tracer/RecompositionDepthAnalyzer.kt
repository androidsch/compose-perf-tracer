package com.composeperf.tracer

/**
 * Tracks the nesting depth at which recompositions occur.
 * Deeper recompositions may indicate poorly scoped state reads.
 */
class RecompositionDepthAnalyzer {

    data class DepthEntry(
        val composableName: String,
        val depth: Int,
        val count: Int
    )

    private val depthMap = mutableMapOf<String, MutableList<Int>>()

    /**
     * Records a recomposition event for [composableName] at the given [depth].
     */
    fun record(composableName: String, depth: Int) {
        require(depth >= 0) { "Depth must be non-negative, was $depth" }
        depthMap.getOrPut(composableName) { mutableListOf() }.add(depth)
    }

    /**
     * Returns the average depth for each tracked composable.
     */
    fun averageDepths(): Map<String, Double> {
        return depthMap.mapValues { (_, depths) -> depths.average() }
    }

    /**
     * Returns composables whose average recomposition depth exceeds [threshold].
     */
    fun deepRecompositions(threshold: Int): List<DepthEntry> {
        return depthMap
            .filter { (_, depths) -> depths.average() > threshold }
            .map { (name, depths) ->
                DepthEntry(
                    composableName = name,
                    depth = depths.max(),
                    count = depths.size
                )
            }
            .sortedByDescending { it.depth }
    }

    /**
     * Returns the maximum recorded depth across all composables.
     */
    fun maxDepth(): Int {
        return depthMap.values.flatten().maxOrNull() ?: 0
    }

    /**
     * Clears all recorded depth data.
     */
    fun reset() {
        depthMap.clear()
    }

    /**
     * Returns the total number of composables being tracked.
     */
    fun trackedCount(): Int = depthMap.size
}
