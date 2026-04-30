package com.composeperf.tracer

import androidx.annotation.VisibleForTesting

/**
 * Represents a single timestamped recomposition event recorded in the timeline.
 */
data class RecompositionTimelineEntry(
    val composableName: String,
    val count: Int,
    val timestampMs: Long
)

/**
 * Records a chronological timeline of recomposition events, allowing
 * consumers to replay or inspect the order in which recompositions occurred.
 */
class RecompositionTimeline(
    private val maxEntries: Int = 500,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    private val _entries = ArrayDeque<RecompositionTimelineEntry>()

    val entries: List<RecompositionTimelineEntry>
        get() = _entries.toList()

    /**
     * Records a recomposition event for the given composable.
     */
    fun record(composableName: String, count: Int = 1) {
        if (_entries.size >= maxEntries) {
            _entries.removeFirst()
        }
        _entries.addLast(
            RecompositionTimelineEntry(
                composableName = composableName,
                count = count,
                timestampMs = clock()
            )
        )
    }

    /**
     * Returns all entries within the given time range (inclusive).
     */
    fun entriesInRange(fromMs: Long, toMs: Long): List<RecompositionTimelineEntry> =
        _entries.filter { it.timestampMs in fromMs..toMs }

    /**
     * Returns entries for a specific composable name.
     */
    fun entriesFor(composableName: String): List<RecompositionTimelineEntry> =
        _entries.filter { it.composableName == composableName }

    /**
     * Clears all recorded timeline entries.
     */
    fun clear() = _entries.clear()

    /**
     * Returns the total number of recorded entries.
     */
    val size: Int get() = _entries.size
}
