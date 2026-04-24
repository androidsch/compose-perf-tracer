package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionHeatMapTest {

    private lateinit var heatMap: RecompositionHeatMap

    @Before
    fun setUp() {
        heatMap = RecompositionHeatMap(
            warmThreshold = 5,
            hotThreshold = 15,
            criticalThreshold = 30
        )
    }

    @Test
    fun `tierFor returns COOL for count below warm threshold`() {
        assertEquals(HeatTier.COOL, heatMap.tierFor(0))
        assertEquals(HeatTier.COOL, heatMap.tierFor(4))
    }

    @Test
    fun `tierFor returns WARM for count between warm and hot thresholds`() {
        assertEquals(HeatTier.WARM, heatMap.tierFor(5))
        assertEquals(HeatTier.WARM, heatMap.tierFor(14))
    }

    @Test
    fun `tierFor returns HOT for count between hot and critical thresholds`() {
        assertEquals(HeatTier.HOT, heatMap.tierFor(15))
        assertEquals(HeatTier.HOT, heatMap.tierFor(29))
    }

    @Test
    fun `tierFor returns CRITICAL for count at or above critical threshold`() {
        assertEquals(HeatTier.CRITICAL, heatMap.tierFor(30))
        assertEquals(HeatTier.CRITICAL, heatMap.tierFor(100))
    }

    @Test
    fun `build returns entries sorted by count descending`() {
        val snapshot = mapOf("ButtonA" to 3, "ScreenB" to 40, "CardC" to 12)
        val entries = heatMap.build(snapshot)
        assertEquals("ScreenB", entries[0].composableName)
        assertEquals("CardC", entries[1].composableName)
        assertEquals("ButtonA", entries[2].composableName)
    }

    @Test
    fun `build assigns correct tiers to entries`() {
        val snapshot = mapOf("A" to 2, "B" to 8, "C" to 20, "D" to 35)
        val entries = heatMap.build(snapshot).associateBy { it.composableName }
        assertEquals(HeatTier.COOL, entries["A"]!!.tier)
        assertEquals(HeatTier.WARM, entries["B"]!!.tier)
        assertEquals(HeatTier.HOT, entries["C"]!!.tier)
        assertEquals(HeatTier.CRITICAL, entries["D"]!!.tier)
    }

    @Test
    fun `summary returns correct counts per tier`() {
        val snapshot = mapOf("A" to 2, "B" to 8, "C" to 20, "D" to 35, "E" to 1)
        val entries = heatMap.build(snapshot)
        val summary = heatMap.summary(entries)
        assertEquals(2, summary[HeatTier.COOL])
        assertEquals(1, summary[HeatTier.WARM])
        assertEquals(1, summary[HeatTier.HOT])
        assertEquals(1, summary[HeatTier.CRITICAL])
    }

    @Test
    fun `build returns empty list for empty snapshot`() {
        val entries = heatMap.build(emptyMap())
        assertTrue(entries.isEmpty())
    }
}
