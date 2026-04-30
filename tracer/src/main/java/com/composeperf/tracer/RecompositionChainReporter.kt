package com.composeperf.tracer

import android.util.Log

/**
 * Reports detected recomposition chains to logcat in a human-readable format.
 */
class RecompositionChainReporter(
    private val tag: String = "RecompositionChain",
    private val logger: (String, String) -> Unit = { t, msg -> Log.w(t, msg) }
) {

    /**
     * Reports all provided [chains] to the logger.
     *
     * @param chains List of [RecompositionChain]s to report.
     */
    fun report(chains: List<RecompositionChain>) {
        if (chains.isEmpty()) {
            logger(tag, "No recomposition chains detected.")
            return
        }
        logger(tag, "Detected ${chains.size} recomposition chain(s):")
        chains.forEachIndexed { index, chain ->
            val chainStr = chain.chain.joinToString(" -> ")
            logger(
                tag,
                "[${index + 1}] Root='${chain.rootComposable}' " +
                    "depth=${chain.depth} total=${chain.totalRecompositions} | $chainStr"
            )
        }
    }

    /**
     * Reports only chains that exceed the given [depthThreshold].
     */
    fun reportDeep(chains: List<RecompositionChain>, depthThreshold: Int = 3) {
        val deep = chains.filter { it.depth >= depthThreshold }
        if (deep.isEmpty()) {
            logger(tag, "No deep recomposition chains (depth >= $depthThreshold) found.")
            return
        }
        logger(tag, "Deep chains (depth >= $depthThreshold): ${deep.size}")
        deep.forEach { chain ->
            logger(
                tag,
                "Deep chain root='${chain.rootComposable}' depth=${chain.depth}: " +
                    chain.chain.joinToString(" -> ")
            )
        }
    }
}
