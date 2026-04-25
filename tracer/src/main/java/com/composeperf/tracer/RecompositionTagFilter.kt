package com.composeperf.tracer

/**
 * A filter that includes or excludes composables based on tag annotations
 * attached via [RecompositionAnnotation].
 */
class RecompositionTagFilter(
    private val includedTags: Set<String> = emptySet(),
    private val excludedTags: Set<String> = emptySet()
) {

    /**
     * Returns true if the composable with the given [name] and [tags] should
     * be included in tracking output.
     *
     * Exclusion takes priority over inclusion. If [includedTags] is empty,
     * all non-excluded composables are included.
     */
    fun shouldInclude(name: String, tags: Set<String>): Boolean {
        if (excludedTags.isNotEmpty() && tags.any { it in excludedTags }) {
            return false
        }
        if (includedTags.isNotEmpty() && tags.none { it in includedTags }) {
            return false
        }
        return true
    }

    /**
     * Filters a map of composable names to recomposition counts, retaining
     * only entries whose associated tags pass the filter.
     *
     * @param counts     map of composable name -> recomposition count
     * @param tagLookup  function that returns the tag set for a composable name
     */
    fun filter(
        counts: Map<String, Int>,
        tagLookup: (String) -> Set<String>
    ): Map<String, Int> {
        return counts.filter { (name, _) ->
            shouldInclude(name, tagLookup(name))
        }
    }

    companion object {
        /** A filter that passes every composable through unchanged. */
        val PASS_ALL = RecompositionTagFilter()
    }
}
