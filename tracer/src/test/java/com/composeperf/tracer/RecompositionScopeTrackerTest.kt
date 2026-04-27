package com.composeperf.tracer

import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionScopeTrackerTest {

    private lateinit var registry: RecompositionRegistry
    private lateinit var alertDispatcher: RecompositionAlertDispatcher
    private lateinit var tracker: RecompositionScopeTracker
    private lateinit var reporter: RecompositionScopeReporter

    private val loggedMessages = mutableListOf<String>()

    @Before
    fun setUp() {
        registry = mockk(relaxed = true)
        alertDispatcher = mockk(relaxed = true)
        tracker = RecompositionScopeTracker(registry, alertDispatcher)
        reporter = RecompositionScopeReporter(
            tracker = tracker,
            logger = { _, msg -> loggedMessages.add(msg) }
        )
    }

    @Test
    fun `record increments count for scope`() {
        tracker.record("HomeScreen")
        tracker.record("HomeScreen")
        assertEquals(2, tracker.countFor("HomeScreen"))
    }

    @Test
    fun `record delegates to registry`() {
        tracker.record("ProfileCard")
        verify { registry.record("ProfileCard") }
    }

    @Test
    fun `record dispatches alert`() {
        tracker.record("FeedList")
        verify { alertDispatcher.dispatch("FeedList", 1) }
    }

    @Test
    fun `snapshot returns copy of all scope counts`() {
        tracker.record("A")
        tracker.record("A")
        tracker.record("B")
        val snap = tracker.snapshot()
        assertEquals(2, snap["A"])
        assertEquals(1, snap["B"])
    }

    @Test
    fun `reset clears all counters`() {
        tracker.record("X")
        tracker.reset()
        assertEquals(0, tracker.countFor("X"))
        assertTrue(tracker.snapshot().isEmpty())
    }

    @Test
    fun `scopesExceeding filters correctly`() {
        tracker.record("Alpha")
        repeat(5) { tracker.record("Beta") }
        val result = tracker.scopesExceeding(3)
        assertTrue(result.containsKey("Beta"))
        assertTrue(!result.containsKey("Alpha"))
    }

    @Test
    fun `reporter reportAll logs all scopes`() {
        tracker.record("Nav")
        tracker.record("Nav")
        tracker.record("Footer")
        reporter.reportAll()
        assertTrue(loggedMessages.any { it.contains("Nav") && it.contains("2") })
        assertTrue(loggedMessages.any { it.contains("Footer") && it.contains("1") })
    }

    @Test
    fun `reporter reportExceeding logs only offenders`() {
        repeat(3) { tracker.record("Header") }
        tracker.record("Sidebar")
        reporter.reportExceeding(2)
        assertTrue(loggedMessages.any { it.contains("Header") })
        assertTrue(loggedMessages.none { it.contains("Sidebar") && it.contains("OVER THRESHOLD") })
    }

    @Test
    fun `reporter summaryText returns sorted key=value pairs`() {
        repeat(4) { tracker.record("Top") }
        repeat(2) { tracker.record("Mid") }
        val summary = reporter.summaryText()
        val lines = summary.lines()
        assertEquals("Top=4", lines[0])
        assertEquals("Mid=2", lines[1])
    }
}
