package com.composeperf.tracer

/**
 * Represents a heat map of recomposition counts, bucketing composables
 * into severity tiers for quick visual analysis.
 */
data class RecompositionHeatMapEntry(
    val composableName: String,
    val count: Int,
    val tier: HeatTier
)

enum class HeatTier(val label: String) {
    COOL("cool"),
    WARM("warm"),
    HOT("hot"),
    CRITICAL("critical")
}

class RecompositionHeatMap(
    private val warmThreshold: Int = 5,
    private val hotThreshold: Int = 15,
    private val criticalThreshold: Int = 30
) {

    fun build(snapshot: Map<String, Int>): List<RecompositionHeatMapEntry> {
        return snapshot.map { (name, count) ->
            RecompositionHeatMapEntry(
                composableName = name,
                count = count,
                tier = tierFor(count)
            )
        }.sortedByDescending { it.count }
    }

    fun tierFor(count: Int): HeatTier = when {
        count >= criticalThreshold -> HeatTier.CRITICAL
        count >= hotThreshold -> HeatTier.HOT
        count >= warmThreshold -> HeatTier.WARM
        else -> HeatTier.COOL
    }

    fun summary(entries: List<RecompositionHeatMapEntry>): Map<HeatTier, Int> {
        return HeatTier.values().associateWith { tier ->
            entries.count { it.tier == tier }
        }
    }
}
