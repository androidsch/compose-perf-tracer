package com.composeperf.tracer

import android.util.Log

/**
 * Interface for handling recomposition log events.
 */
interface RecompositionLogger {
    /**
     * Called on every recomposition of a tracked composable.
     *
     * @param name  The composable identifier.
     * @param count The total number of recompositions so far.
     */
    fun log(name: String, count: Int)

    /**
     * Called when a tracked composable leaves the composition.
     *
     * @param name       The composable identifier.
     * @param totalCount The final recomposition count.
     */
    fun onDispose(name: String, totalCount: Int)
}

/**
 * Default logger that writes to Android Logcat under the tag "RecompositionTracker".
 * Only active in debug builds — replace or disable in release via ProGuard or build config.
 */
object DefaultRecompositionLogger : RecompositionLogger {

    private const val TAG = "RecompositionTracker"

    override fun log(name: String, count: Int) {
        if (count > 1) {
            Log.d(TAG, "[$name] recomposed $count time(s)")
        }
    }

    override fun onDispose(name: String, totalCount: Int) {
        Log.d(TAG, "[$name] disposed after $totalCount recomposition(s)")
    }
}

/**
 * A no-op logger suitable for release builds or when tracking should be silenced.
 */
object NoOpRecompositionLogger : RecompositionLogger {
    override fun log(name: String, count: Int) = Unit
    override fun onDispose(name: String, totalCount: Int) = Unit
}
