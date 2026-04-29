package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionFrequencyAnalyzerTest {

    private lateinit var analyzer: RecompositionFrequencyAnalyzer

    @Before
    fun setUp() {
        // 10-second window for easier arithmetic in tests
        analyzer = RecompositionFrequencyAnalyzer(windowMillis = 10_000L)
    }

    @Test
    fun `frequencyOf returns 0 when no events recorded`() {
        assertEquals(0.0, analyzer.frequencyOf("Screen"), 0.001)
    }

    @Test
    fun `frequencyOf calculates correct rps within window`() {
        val now = 100_000L
        // Record 10 events spread within the 10-second window
        repeat(10) { i ->
            analyzer.record("Screen", now - 9_000L + i * 1_000L)
        }
        val freq = analyzer.frequencyOf("Screen", now)
        // 10 events / 10 seconds = 1.0 rps
        assertEquals(1.0, freq, 0.001)
    }

    @Test
    fun `frequencyOf excludes events outside the window`() {
        val now = 100_000L
        // 5 events inside window, 5 outside
        repeat(5) { analyzer.record("Card", now - 15_000L + it * 1_000L) } // outside
        repeat(5) { analyzer.record("Card", now - 4_000L + it * 1_000L) }  // inside
        val freq = analyzer.frequencyOf("Card", now)
        // 5 events / 10 seconds = 0.5 rps
        assertEquals(0.5, freq, 0.001)
    }

    @Test
    fun `allFrequencies returns entry for each tracked composable`() {
        val now = 200_000L
        analyzer.record("A", now - 1_000L)
        analyzer.record("B", now - 2_000L)
        analyzer.record("B", now - 1_000L)
        val all = analyzer.allFrequencies(now)
        assertTrue(all.containsKey("A"))
        assertTrue(all.containsKey("B"))
        assertTrue(all["B"]!! > all["A"]!!)
    }

    @Test
    fun `hotComposables filters by threshold`() {
        val now = 300_000L
        // "Heavy" gets 20 events -> 2.0 rps; "Light" gets 5 -> 0.5 rps
        repeat(20) { analyzer.record("Heavy", now - 9_500L + it * 500L) }
        repeat(5)  { analyzer.record("Light", now - 4_000L + it * 1_000L) }
        val hot = analyzer.hotComposables(threshold = 1.0, nowMillis = now)
        assertTrue(hot.containsKey("Heavy"))
        assertFalse(hot.containsKey("Light"))
    }

    @Test
    fun `reset clears all recorded events`() {
        val now = 400_000L
        analyzer.record("X", now - 1_000L)
        analyzer.reset()
        assertEquals(0.0, analyzer.frequencyOf("X", now), 0.001)
        assertTrue(analyzer.allFrequencies(now).isEmpty())
    }

    @Test
    fun `frequencyOf returns 0 after all events expire from window`() {
        val past = 500_000L
        analyzer.record("Stale", past - 9_000L)
        // Move time forward so all events are outside the window
        val future = past + 5_000L
        assertEquals(0.0, analyzer.frequencyOf("Stale", future), 0.001)
    }
}
