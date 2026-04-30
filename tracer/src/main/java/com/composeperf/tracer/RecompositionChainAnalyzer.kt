package com.composeperf.tracer

/**
 * Represents a chain of recompositions where one composable triggers recompositions
 * in its descendants within a single frame.
 */
data class RecompositionChain(
    val rootComposable: String,
    val chain: List<String>,
    val totalRecompositions: Int,
    val depth: Int
)

/**
 * Analyzes recomposition data to detect propagation chains — sequences of composables
 * that recompose together, suggesting a root cause triggering cascading recompositions.
 */
class RecompositionChainAnalyzer(
    private val minChainLength: Int = 2,
    private val minTotalRecompositions: Int = 3
) {

    /**
     * Analyzes the given snapshot map to find recomposition chains.
     *
     * @param snapshots Map of composable name to its [RecompositionSnapshot].
     * @param parentMap Map of composable name to its parent composable name.
     * @return List of detected [RecompositionChain]s sorted by depth descending.
     */
    fun analyze(
        snapshots: Map<String, RecompositionSnapshot>,
        parentMap: Map<String, String>
    ): List<RecompositionChain> {
        val childrenMap = buildChildrenMap(parentMap, snapshots.keys)
        val roots = snapshots.keys.filter { it !in parentMap }

        return roots
            .mapNotNull { root -> buildChain(root, snapshots, childrenMap) }
            .filter { it.chain.size >= minChainLength && it.totalRecompositions >= minTotalRecompositions }
            .sortedByDescending { it.depth }
    }

    private fun buildChildrenMap(
        parentMap: Map<String, String>,
        allKeys: Set<String>
    ): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        for (key in allKeys) {
            val parent = parentMap[key] ?: continue
            map.getOrPut(parent) { mutableListOf() }.add(key)
        }
        return map
    }

    private fun buildChain(
        node: String,
        snapshots: Map<String, RecompositionSnapshot>,
        childrenMap: Map<String, List<String>>
    ): RecompositionChain? {
        val chain = mutableListOf<String>()
        var current = node
        while (true) {
            val snapshot = snapshots[current] ?: break
            if (snapshot.count == 0) break
            chain.add(current)
            val children = childrenMap[current]
            val next = children?.maxByOrNull { snapshots[it]?.count ?: 0 }
            if (next == null || (snapshots[next]?.count ?: 0) == 0) break
            current = next
        }
        if (chain.isEmpty()) return null
        val total = chain.sumOf { snapshots[it]?.count ?: 0 }
        return RecompositionChain(
            rootComposable = chain.first(),
            chain = chain,
            totalRecompositions = total,
            depth = chain.size
        )
    }
}
