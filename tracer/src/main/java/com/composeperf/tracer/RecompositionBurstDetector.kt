package com.composeperf.tracer

/**
 * Detects bursts of recompositions for composables within a sliding time window.
 *
 * A burst is defined as [burstThreshold] or more recompositions occurring
 * within [windowMs] milliseconds for the same composable.
 */
class RecompositionBurstDetector(
    private val windowMs: Long = 500L,
    private val burstThreshold: Int = 5,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val eventLog = mutableMapOf<String, ArrayDeque<Long>>()

    /**
     * Records a recomposition event for [composableName] and returns a [RecompositionBurst]
     * if a burst is detected, or null otherwise.
     */
    fun record(composableName: String): RecompositionBurst? {
        val now = clock()
        val timestamps = eventLog.getOrPut(composableName) { ArrayDeque() }
        timestamps.addLast(now)

        // Evict events outside the window
        while (timestamps.isNotEmpty() && now - timestamps.first() > windowMs) {
            timestamps.removeFirst()
        }

        return if (timestamps.size >= burstThreshold) {
            RecompositionBurst(
                composableName = composableName,
                count = timestamps.size,
                windowMs = windowMs,
                startTimestamp = timestamps.first(),
                endTimestamp = now
            )
        } else null
    }

    /** Returns all composables that are currently in a burst state. */
    fun activeBursts(): List<RecompositionBurst> {
        val now = clock()
        return eventLog.entries.mapNotNull { (name, timestamps) ->
            val recent = timestamps.filter { now - it <= windowMs }
            if (recent.size >= burstThreshold) {
                RecompositionBurst(
                    composableName = name,
                    count = recent.size,
                    windowMs = windowMs,
                    startTimestamp = recent.first(),
                    endTimestamp = recent.last()
                )
            } else null
        }
    }

    /** Clears all recorded events. */
    fun reset() = eventLog.clear()
}
