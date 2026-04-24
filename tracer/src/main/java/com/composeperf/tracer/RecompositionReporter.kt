package com.composeperf.tracer

/**
 * Builds a [RecompositionReport] from the current state of [RecompositionRegistry]
 * and formats it for human-readable output.
 */
class RecompositionReporter(
    private val registry: RecompositionRegistry,
    private val threshold: RecompositionThreshold = RecompositionThreshold()
) {

    /**
     * Generates a snapshot report from all currently tracked composables.
     */
    fun generateReport(): RecompositionReport {
        val snapshots = registry.getAllCounts()
            .map { (name, count) -> RecompositionSnapshot(composableName = name, recompositionCount = count) }
            .filter { it.recompositionCount >= threshold.warningLevel }
        return RecompositionReport(snapshots = snapshots)
    }

    /**
     * Formats a [RecompositionReport] into a multi-line string suitable for logging.
     */
    fun format(report: RecompositionReport): String {
        if (report.isEmpty) return "[RecompositionReporter] No recompositions above threshold."

        val sb = StringBuilder()
        sb.appendLine("[RecompositionReporter] Recomposition Report @ ${report.generatedAtMs}ms")
        sb.appendLine("  Total recompositions : ${report.totalRecompositions}")
        sb.appendLine("  Tracked composables  : ${report.snapshots.size}")
        sb.appendLine("  Hotspots (top 5):")
        report.topN(5).forEachIndexed { index, snapshot ->
            val indicator = if (snapshot.recompositionCount >= threshold.criticalLevel) "🔴" else "🟡"
            sb.appendLine("    ${index + 1}. $indicator ${snapshot.composableName} — ${snapshot.recompositionCount}x")
        }
        return sb.toString().trimEnd()
    }
}
