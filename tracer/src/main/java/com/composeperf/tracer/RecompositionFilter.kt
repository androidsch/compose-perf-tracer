package com.composeperf.tracer

/**
 * Defines filtering strategies for recomposition events.
 * Allows consumers to suppress noise from known or acceptable recompositions.
 */
interface RecompositionFilter {
    /**
     * Returns true if the recomposition event for [composableName] should be
     * included (i.e. not filtered out).
     */
    fun shouldInclude(composableName: String, count: Int): Boolean
}

/**
 * A filter that excludes composables whose names match any of the given [excludedPrefixes].
 */
class PrefixExclusionFilter(
    private val excludedPrefixes: Set<String>
) : RecompositionFilter {
    override fun shouldInclude(composableName: String, count: Int): Boolean {
        return excludedPrefixes.none { prefix -> composableName.startsWith(prefix) }
    }
}

/**
 * A filter that only includes composables whose recomposition [count] meets or
 * exceeds [minimumCount]. Useful for suppressing single-recomposition noise.
 */
class MinCountFilter(
    private val minimumCount: Int
) : RecompositionFilter {
    init {
        require(minimumCount >= 1) { "minimumCount must be at least 1, was $minimumCount" }
    }

    override fun shouldInclude(composableName: String, count: Int): Boolean {
        return count >= minimumCount
    }
}

/**
 * Combines multiple [RecompositionFilter] instances. An event is included only
 * when ALL filters approve it (logical AND).
 */
class CompositeFilter(
    private val filters: List<RecompositionFilter>
) : RecompositionFilter {
    override fun shouldInclude(composableName: String, count: Int): Boolean {
        return filters.all { it.shouldInclude(composableName, count) }
    }

    companion object {
        fun of(vararg filters: RecompositionFilter): CompositeFilter =
            CompositeFilter(filters.toList())
    }
}

/** A no-op filter that always includes every recomposition event. */
object PassThroughFilter : RecompositionFilter {
    override fun shouldInclude(composableName: String, count: Int): Boolean = true
}
