package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionTimelineTest {

    private var fakeTime = 1000L
    private lateinit var timeline: RecompositionTimeline

    @Before
    fun setUp() {
        fakeTime = 1000L
        timeline = RecompositionTimeline(maxEntries = 5, clock = { fakeTime })
    }

    @Test
    fun `record adds an entry with correct fields`() {
        fakeTime = 2000L
        timeline.record("ButtonComposable", count = 3)

        val entries = timeline.entries
        assertEquals(1, entries.size)
        assertEquals("ButtonComposable", entries[0].composableName)
        assertEquals(3, entries[0].count)
        assertEquals(2000L, entries[0].timestampMs)
    }

    @Test
    fun `record evicts oldest entry when maxEntries exceeded`() {
        repeat(6) { i ->
            fakeTime = (1000L + i * 100)
            timeline.record("Comp$i")
        }
        assertEquals(5, timeline.size)
        // First entry should be Comp1 (Comp0 was evicted)
        assertEquals("Comp1", timeline.entries.first().composableName)
    }

    @Test
    fun `entriesInRange returns only entries within range`() {
        listOf(500L, 1000L, 1500L, 2000L, 2500L).forEachIndexed { i, ts ->
            fakeTime = ts
            timeline.record("Comp$i")
        }
        val result = timeline.entriesInRange(1000L, 2000L)
        assertEquals(3, result.size)
        assertTrue(result.all { it.timestampMs in 1000L..2000L })
    }

    @Test
    fun `entriesFor returns only matching composable entries`() {
        fakeTime = 1000L; timeline.record("Alpha")
        fakeTime = 1100L; timeline.record("Beta")
        fakeTime = 1200L; timeline.record("Alpha")

        val result = timeline.entriesFor("Alpha")
        assertEquals(2, result.size)
        assertTrue(result.all { it.composableName == "Alpha" })
    }

    @Test
    fun `clear removes all entries`() {
        timeline.record("X")
        timeline.record("Y")
        timeline.clear()
        assertEquals(0, timeline.size)
        assertTrue(timeline.entries.isEmpty())
    }

    @Test
    fun `entriesInRange returns empty list when no entries match`() {
        fakeTime = 5000L
        timeline.record("LateComp")
        val result = timeline.entriesInRange(1000L, 2000L)
        assertTrue(result.isEmpty())
    }
}
