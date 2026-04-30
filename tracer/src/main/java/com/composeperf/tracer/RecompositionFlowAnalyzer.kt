package com.composeperf.tracer

/**
 * Analyzes the flow of recompositions across a parent-child composable hierarchy,
 * identifying propagation paths that trigger cascading recompositions.
 */
class RecompositionFlowAnalyzer {

    private val flowEdges = mutableMapOf<String, MutableSet<String>>()
    private val propagationCounts = mutableMapOf<String, Int>()

    /**
     * Records that a recomposition in [parent] triggered a recomposition in [child].
     */
    fun recordPropagation(parent: String, child: String) {
        flowEdges.getOrPut(parent) { mutableSetOf() }.add(child)
        propagationCounts[parent] = (propagationCounts[parent] ?: 0) + 1
    }

    /**
     * Returns all direct children that [composable] propagates recompositions to.
     */
    fun childrenOf(composable: String): Set<String> =
        flowEdges[composable] ?: emptySet()

    /**
     * Returns composables sorted by how many times they triggered downstream recompositions.
     */
    fun topPropagators(limit: Int = 10): List<Pair<String, Int>> =
        propagationCounts.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }

    /**
     * Performs a depth-first traversal from [root] and returns the full propagation chain.
     */
    fun propagationChain(root: String): List<String> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<String>()
        fun dfs(node: String) {
            if (!visited.add(node)) return
            result.add(node)
            flowEdges[node]?.forEach { dfs(it) }
        }
        dfs(root)
        return result
    }

    /**
     * Returns the total number of unique propagation edges recorded.
     */
    fun edgeCount(): Int = flowEdges.values.sumOf { it.size }

    /**
     * Clears all recorded flow data.
     */
    fun reset() {
        flowEdges.clear()
        propagationCounts.clear()
    }
}
