package com.composeperf.tracer

/**
 * Marks a composable function for recomposition tracking.
 *
 * When [enabled] is true and a [label] is provided, the tracker will use the label
 * instead of the function name for grouping and reporting purposes.
 *
 * Usage:
 * ```
 * @TrackRecomposition(label = "HomeScreen")
 * @Composable
 * fun HomeScreen() { ... }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class TrackRecomposition(
    val label: String = "",
    val enabled: Boolean = true
)

/**
 * Marks a composable as expected to recompose frequently.
 * Suppresses alerts for composables that legitimately recompose often
 * (e.g., animations, real-time data displays).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class HighFrequencyComposable(
    val reason: String = ""
)

/**
 * Marks a composable as stable, meaning it should rarely or never recompose.
 * If recompositions exceed [maxAllowed], an alert will be dispatched.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class StableComposable(
    val maxAllowed: Int = 1
)
