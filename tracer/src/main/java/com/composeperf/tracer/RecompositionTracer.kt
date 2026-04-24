package com.composeperf.tracer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Main entry point for the Compose recomposition tracer.
 *
 * Use [TraceRecompositions] composable to wrap any composable you want to monitor,
 * or call [RecompositionTracer.init] once in your Application class to configure
 * global settings.
 *
 * Example usage:
 * ```
 * TraceRecompositions(label = "MyScreen") {
 *     MyScreen()
 * }
 * ```
 */
object RecompositionTracer {

    private var isEnabled: Boolean = false
    private lateinit var registry: RecompositionRegistry
    private lateinit var alertDispatcher: RecompositionAlertDispatcher
    private lateinit var threshold: RecompositionThreshold
    private lateinit var logger: RecompositionLogger

    /**
     * Initializes the tracer with the provided configuration.
     * Should be called once, typically from your Application.onCreate().
     *
     * @param enabled Whether tracing is active. Defaults to true only in debug builds.
     * @param threshold Configuration for when to trigger alerts.
     * @param logger Custom logger implementation. Defaults to [RecompositionLogger].
     * @param alertDispatcher Dispatcher responsible for emitting alerts.
     */
    fun init(
        enabled: Boolean = true,
        threshold: RecompositionThreshold = RecompositionThreshold(),
        logger: RecompositionLogger = RecompositionLogger(),
        alertDispatcher: RecompositionAlertDispatcher = RecompositionAlertDispatcher(logger)
    ) {
        this.isEnabled = enabled
        this.threshold = threshold
        this.logger = logger
        this.alertDispatcher = alertDispatcher
        this.registry = RecompositionRegistry(threshold, alertDispatcher)
    }

    /**
     * Returns true if the tracer has been initialized and is currently enabled.
     */
    fun isActive(): Boolean = isEnabled && ::registry.isInitialized

    /**
     * Records a recomposition event for the given label.
     * No-op if the tracer is not active.
     *
     * @param label A unique identifier for the composable being tracked.
     */
    internal fun record(label: String) {
        if (!isActive()) return
        registry.record(label)
    }

    /**
     * Resets all recorded recomposition counts.
     * Useful between test scenarios or navigation events.
     */
    fun reset() {
        if (::registry.isInitialized) {
            registry.reset()
        }
    }

    /**
     * Returns a snapshot of current recomposition counts keyed by label.
     */
    fun snapshot(): Map<String, Int> {
        if (!::registry.isInitialized) return emptyMap()
        return registry.snapshot()
    }
}

/**
 * A composable wrapper that tracks recompositions of its [content] block.
 *
 * Each time [content] recomposes, the count for [label] is incremented in the
 * [RecompositionTracer] registry. If the count exceeds the configured threshold,
 * an alert is dispatched via [RecompositionAlertDispatcher].
 *
 * This composable is a no-op when [RecompositionTracer] is not active.
 *
 * @param label A descriptive name for the composable being tracked.
 * @param content The composable content to monitor.
 */
@Composable
fun TraceRecompositions(
    label: String,
    content: @Composable () -> Unit
) {
    if (RecompositionTracer.isActive()) {
        val counter = remember(label) { RecompositionCounter(label) }
        val currentLabel = rememberUpdatedState(label)

        // Record a recomposition each time this block executes
        counter.increment()
        RecompositionTracer.record(currentLabel.value)

        DisposableEffect(label) {
            onDispose {
                // Optionally log final count on disposal
            }
        }
    }

    content()
}
