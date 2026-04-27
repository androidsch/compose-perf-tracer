package com.composeperf.tracer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Tracks recomposition counts scoped to a named composable region.
 * Useful for isolating which UI scope is recomposing and how often.
 */
class RecompositionScopeTracker(
    private val registry: RecompositionRegistry,
    private val alertDispatcher: RecompositionAlertDispatcher? = null
) {

    private val scopeCounters = mutableMapOf<String, Int>()

    /**
     * Records a recomposition event for the given scope name.
     */
    fun record(scopeName: String) {
        val newCount = (scopeCounters[scopeName] ?: 0) + 1
        scopeCounters[scopeName] = newCount
        registry.record(scopeName)
        alertDispatcher?.dispatch(scopeName, newCount)
    }

    /**
     * Returns current recomposition count for a given scope.
     */
    fun countFor(scopeName: String): Int = scopeCounters[scopeName] ?: 0

    /**
     * Returns a snapshot of all tracked scopes and their counts.
     */
    fun snapshot(): Map<String, Int> = scopeCounters.toMap()

    /**
     * Resets all scope counters.
     */
    fun reset() {
        scopeCounters.clear()
    }

    /**
     * Returns all scopes that exceeded the given threshold.
     */
    fun scopesExceeding(threshold: Int): Map<String, Int> =
        scopeCounters.filter { (_, count) -> count > threshold }
}

/**
 * Composable helper that automatically records recompositions for the given [scopeName].
 * Should be called at the top of a tracked composable.
 */
@Composable
fun TrackRecompositionScope(
    scopeName: String,
    tracker: RecompositionScopeTracker
) {
    val counter = remember(scopeName) { tracker }
    counter.record(scopeName)

    DisposableEffect(scopeName) {
        onDispose { /* scope left composition, no-op for now */ }
    }
}
