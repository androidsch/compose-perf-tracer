package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionTimelineReporterTest {

    private var fakeTime = 1000L
    private lateinit var timeline: RecompositionTimeline
    private val logLines = mutableListOf<String>()
    private lateinit var reporter: RecompositionTimelineReporter

    @Before
    fun setUp() {
        fakeTime = 1000L
        logLines.clear()
        timeline = RecompositionTimeline(clock = { fakeTime })
        reporter = RecompositionTimelineReporter(
            timeline = timeline,
            tag = "TEST",
            logger = { _, msg -> logLines.add(msg) }
        )
    }

    @Test
    fun `reportAll logs empty message when timeline is empty`() {
        reporter.reportAll()
        assertEquals(1, logLines.size)
        assertTrue(logLines[0].contains("empty"))
    }

    @Test
    fun `reportAll logs all entries`() {
        fakeTime = 1000L; timeline.record("Foo", 2)
        fakeTime = 2000L; timeline.record("Bar", 5)

        reporter.reportAll()

        assertTrue(logLines.any { it.contains("2 entries") })
        assertTrue(logLines.any { it.contains("Foo") && it.contains("count: 2") })
        assertTrue(logLines.any { it.contains("Bar") && it.contains("count: 5") })
    }

    @Test
    fun `reportFor logs not-found message when composable absent`() {
        reporter.reportFor("Missing")
        assertEquals(1, logLines.size)
        assertTrue(logLines[0].contains("Missing"))
    }

    @Test
    fun `reportFor logs only matching entries`() {
        fakeTime = 1000L; timeline.record("Alpha", 1)
        fakeTime = 1100L; timeline.record("Beta", 3)
        fakeTime = 1200L; timeline.record("Alpha", 2)

        reporter.reportFor("Alpha")

        assertTrue(logLines.any { it.contains("2 entries") })
        assertTrue(logLines.none { it.contains("Beta") })
    }

    @Test
    fun `reportInRange logs entries within range`() {
        fakeTime = 500L;  timeline.record("Early")
        fakeTime = 1500L; timeline.record("Mid")
        fakeTime = 3000L; timeline.record("Late")

        reporter.reportInRange(1000L, 2000L)

        assertTrue(logLines.any { it.contains("1 entries") })
        assertTrue(logLines.any { it.contains("Mid") })
        assertTrue(logLines.none { it.contains("Early") })
        assertTrue(logLines.none { it.contains("Late") })
    }

    @Test
    fun `reportInRange logs empty message when no entries in range`() {
        fakeTime = 9999L; timeline.record("Far")
        reporter.reportInRange(1000L, 2000L)
        assertEquals(1, logLines.size)
        assertTrue(logLines[0].contains("No timeline entries in range"))
    }
}
