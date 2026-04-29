package com.composeperf.tracer

import androidx.compose.runtime.Stable

/**
 * Represents a recorded cause entry for a recomposition event.
 */
data class RecompositionCauseEntry(
    val composableName: String,
    val cause: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tracks the reported causes of recompositions per composable.
 * Callers supply a human-readable cause string (e.g. the name of the state
 * that changed) alongside the composable key so that hot-paths can be
 * diagnosed beyond raw counts.
 */
@Stable
class RecompositionCauseTracker {

    private val _entries = mutableListOf<RecompositionCauseEntry>()

    /** Record a cause for [composableName]. */
    fun record(composableName: String, cause: String) {
        _entries.add(RecompositionCauseEntry(composableName, cause))
    }

    /** Returns all recorded entries in insertion order. */
    fun allEntries(): List<RecompositionCauseEntry> = _entries.toList()

    /**
     * Returns entries for a specific [composableName], optionally limited to
     * the most recent [limit] entries.
     */
    fun entriesFor(composableName: String, limit: Int = Int.MAX_VALUE): List<RecompositionCauseEntry> =
        _entries
            .filter { it.composableName == composableName }
            .takeLast(limit)

    /**
     * Returns a map of composable name → distinct causes with their occurrence
     * counts, sorted by total occurrences descending.
     */
    fun causeFrequency(): Map<String, Map<String, Int>> {
        return _entries
            .groupBy { it.composableName }
            .mapValues { (_, entries) ->
                entries
                    .groupingBy { it.cause }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }
            }
    }

    /** Clears all recorded entries. */
    fun reset() {
        _entries.clear()
    }

    /** Returns the total number of recorded cause entries. */
    val size: Int get() = _entries.size
}
