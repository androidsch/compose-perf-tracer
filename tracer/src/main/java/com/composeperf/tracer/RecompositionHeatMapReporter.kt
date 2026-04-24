package com.composeperf.tracer

/**
 * Reports the recomposition heat map to a provided output sink.
 * Formats entries by tier with counts for developer-friendly output.
 */
class RecompositionHeatMapReporter(
    private val heatMap: RecompositionHeatMap = RecompositionHeatMap(),
    private val output: (String) -> Unit = { android.util.Log.d("HeatMapReporter", it) }
) {

    fun report(snapshot: Map<String, Int>) {
        if (snapshot.isEmpty()) {
            output("[HeatMap] No recomposition data available.")
            return
        }

        val entries = heatMap.build(snapshot)
        val summary = heatMap.summary(entries)

        output("[HeatMap] Recomposition Heat Map")
        output("[HeatMap] ================================")

        HeatTier.values().reversed().forEach { tier ->
            val tierEntries = entries.filter { it.tier == tier }
            if (tierEntries.isNotEmpty()) {
                output("[HeatMap] --- ${tier.label.uppercase()} (${tierEntries.size}) ---")
                tierEntries.forEach { entry ->
                    output("[HeatMap]   ${entry.composableName}: ${entry.count}")
                }
            }
        }

        output("[HeatMap] ================================")
        output("[HeatMap] Summary: " + summary.entries.joinToString(", ") {
            "${it.key.label}=${it.value}"
        })
    }
}
