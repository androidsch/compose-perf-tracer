package com.composeperf.tracer

/**
 * Detects recurring recomposition patterns across composables,
 * such as periodic spikes or consistent co-recomposition of sibling nodes.
 */
class RecompositionPatternDetector(
    private val minOccurrences: Int = 3,
    private val windowSize: Int = 10
) {

    data class Pattern(
        val composables: List<String>,
        val occurrences: Int,
        val description: String
    )

    // Stores recent recomposition events as ordered lists of composable names
    private val recentWindows: ArrayDeque<List<String>> = ArrayDeque()

    /**
     * Records a batch of composables that recomposed together in one frame/pass.
     */
    fun recordBatch(composables: List<String>) {
        if (composables.isEmpty()) return
        recentWindows.addLast(composables.sorted())
        if (recentWindows.size > windowSize) {
            recentWindows.removeFirst()
        }
    }

    /**
     * Detects patterns that have occurred at least [minOccurrences] times
     * within the tracked window.
     */
    fun detectPatterns(): List<Pattern> {
        val frequency = mutableMapOf<List<String>, Int>()
        for (window in recentWindows) {
            frequency[window] = (frequency[window] ?: 0) + 1
        }
        return frequency
            .filter { it.value >= minOccurrences }
            .map { (composables, count) ->
                Pattern(
                    composables = composables,
                    occurrences = count,
                    description = "Co-recomposition pattern detected: ${composables.joinToString(", ")} ($count times)"
                )
            }
            .sortedByDescending { it.occurrences }
    }

    /**
     * Clears all tracked windows.
     */
    fun reset() {
        recentWindows.clear()
    }
}
