package com.composeperf.tracer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Tracks recomposition counts for a named composable scope.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     TrackRecompositions("MyScreen") {
 *         // your content
 *     }
 * }
 * ```
 */
@Composable
fun TrackRecompositions(
    name: String,
    logger: RecompositionLogger = DefaultRecompositionLogger,
    content: @Composable () -> Unit
) {
    val counter = remember(name) { RecompositionCounter(name) }

    SideEffect {
        counter.increment()
        logger.log(counter.name, counter.count)
    }

    DisposableEffect(name) {
        onDispose {
            logger.onDispose(counter.name, counter.count)
            RecompositionRegistry.remove(name)
        }
    }

    RecompositionRegistry.register(counter)
    content()
}

/**
 * Holds the recomposition count for a single composable scope.
 */
class RecompositionCounter(val name: String) {
    private var _count = 0
    val count: Int get() = _count

    fun increment() {
        _count++
    }
}
