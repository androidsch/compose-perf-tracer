package com.composeperf.tracer

/**
 * Configuration for recomposition warning thresholds.
 *
 * @param warnThreshold Number of recompositions before a WARN level log is emitted.
 * @param errorThreshold Number of recompositions before an ERROR level log is emitted.
 * @param windowMs Time window in milliseconds over which recompositions are counted.
 */
data class RecompositionThreshold(
    val warnThreshold: Int = 5,
    val errorThreshold: Int = 10,
    val windowMs: Long = 1_000L
) {
    init {
        require(warnThreshold > 0) { "warnThreshold must be positive" }
        require(errorThreshold > warnThreshold) {
            "errorThreshold ($errorThreshold) must be greater than warnThreshold ($warnThreshold)"
        }
        require(windowMs > 0) { "windowMs must be positive" }
    }

    companion object {
        /** Permissive defaults suitable for most screens. */
        val DEFAULT = RecompositionThreshold()

        /** Strict defaults for performance-critical composables. */
        val STRICT = RecompositionThreshold(warnThreshold = 2, errorThreshold = 5, windowMs = 500L)
    }
}

/**
 * Severity level derived from comparing a recomposition count against a [RecompositionThreshold].
 */
enum class RecompositionSeverity {
    OK,
    WARN,
    ERROR;

    companion object {
        fun from(count: Int, threshold: RecompositionThreshold): RecompositionSeverity = when {
            count >= threshold.errorThreshold -> ERROR
            count >= threshold.warnThreshold -> WARN
            else -> OK
        }
    }
}
