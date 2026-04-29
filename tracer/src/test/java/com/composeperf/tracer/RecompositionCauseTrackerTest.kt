package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionCauseTrackerTest {

    private lateinit var tracker: RecompositionCauseTracker

    @Before
    fun setUp() {
        tracker = RecompositionCauseTracker()
    }

    @Test
    fun `record adds an entry`() {
        tracker.record("MyComposable", "counterState")
        assertEquals(1, tracker.size)
    }

    @Test
    fun `allEntries returns entries in insertion order`() {
        tracker.record("A", "cause1")
        tracker.record("B", "cause2")
        tracker.record("A", "cause3")

        val entries = tracker.allEntries()
        assertEquals(3, entries.size)
        assertEquals("A", entries[0].composableName)
        assertEquals("B", entries[1].composableName)
        assertEquals("A", entries[2].composableName)
    }

    @Test
    fun `entriesFor filters by composable name`() {
        tracker.record("Widget", "stateA")
        tracker.record("Other", "stateB")
        tracker.record("Widget", "stateC")

        val widgetEntries = tracker.entriesFor("Widget")
        assertEquals(2, widgetEntries.size)
        assertTrue(widgetEntries.all { it.composableName == "Widget" })
    }

    @Test
    fun `entriesFor respects limit parameter`() {
        repeat(5) { tracker.record("Card", "scroll") }

        val limited = tracker.entriesFor("Card", limit = 3)
        assertEquals(3, limited.size)
    }

    @Test
    fun `causeFrequency returns correct counts`() {
        tracker.record("Screen", "userInput")
        tracker.record("Screen", "userInput")
        tracker.record("Screen", "networkData")
        tracker.record("Header", "themeChange")

        val freq = tracker.causeFrequency()

        assertEquals(2, freq.size)
        assertEquals(2, freq["Screen"]?.get("userInput"))
        assertEquals(1, freq["Screen"]?.get("networkData"))
        assertEquals(1, freq["Header"]?.get("themeChange"))
    }

    @Test
    fun `causeFrequency sorts causes by count descending`() {
        tracker.record("Box", "rare")
        tracker.record("Box", "frequent")
        tracker.record("Box", "frequent")
        tracker.record("Box", "frequent")

        val causes = tracker.causeFrequency()["Box"] ?: fail("Box not found")
        val keys = causes.keys.toList()
        assertEquals("frequent", keys[0])
        assertEquals("rare", keys[1])
    }

    @Test
    fun `reset clears all entries`() {
        tracker.record("X", "cause")
        tracker.record("Y", "cause")
        tracker.reset()

        assertEquals(0, tracker.size)
        assertTrue(tracker.allEntries().isEmpty())
    }

    @Test
    fun `entriesFor returns empty list for unknown composable`() {
        tracker.record("Known", "c")
        val result = tracker.entriesFor("Unknown")
        assertTrue(result.isEmpty())
    }
}
