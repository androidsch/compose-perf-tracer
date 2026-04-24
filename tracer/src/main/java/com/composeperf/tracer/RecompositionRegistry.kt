package com.composeperf.tracer

import java.util.concurrent.ConcurrentHashMap

/**
 * A global registry that maintains all active [RecompositionCounter] instances.
 * Provides a snapshot of current recomposition counts across all tracked composables.
 */
object RecompositionRegistry {

    private val counters = ConcurrentHashMap<String, RecompositionCounter>()

    /**
     * Registers or replaces a counter for the given composable name.
     */
    fun register(counter: RecompositionCounter) {
        counters[counter.name] = counter
    }

    /**
     * Removes the counter associated with [name].
     */
    fun remove(name: String) {
        counters.remove(name)
    }

    /**
     * Returns a snapshot of all current recomposition counts, keyed by composable name.
     */
    fun snapshot(): Map<String, Int> {
        return counters.mapValues { it.value.count }
    }

    /**
     * Returns the recomposition count for a specific composable, or null if not tracked.
     */
    fun getCount(name: String): Int? = counters[name]?.count

    /**
     * Clears all registered counters. Useful for testing.
     */
    fun clear() {
        counters.clear()
    }
}
