package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionTrendAnalyzerTest {

    private lateinit var analyzer: RecompositionTrendAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionTrendAnalyzer(windowSize = 5)
    }

    @Test
    fun `analyze returns STABLE when only one sample recorded`() {
        analyzer.record("HomeScreen", 3)
        val result = analyzer.analyze("HomeScreen")
        assertEquals(RecompositionTrendAnalyzer.TrendDirection.STABLE, result.direction)
        assertEquals(0.0, result.slope, 0.001)
    }

    @Test
    fun `analyze returns INCREASING for consistently rising counts`() {
        listOf(1, 3, 5, 7, 9).forEach { analyzer.record("FeedList", it) }
        val result = analyzer.analyze("FeedList")
        assertEquals(RecompositionTrendAnalyzer.TrendDirection.INCREASING, result.direction)
        assertTrue(result.slope > 0.5)
    }

    @Test
    fun `analyze returns DECREASING for consistently falling counts`() {
        listOf(10, 8, 6, 4, 2).forEach { analyzer.record("Header", it) }
        val result = analyzer.analyze("Header")
        assertEquals(RecompositionTrendAnalyzer.TrendDirection.DECREASING, result.direction)
        assertTrue(result.slope < -0.5)
    }

    @Test
    fun `analyzeAll returns results for all recorded composables`() {
        listOf(1, 2, 3).forEach { analyzer.record("A", it) }
        listOf(5, 5, 5).forEach { analyzer.record("B", it) }
        val results = analyzer.analyzeAll()
        assertEquals(2, results.size)
        assertTrue(results.any { it.composableName == "A" })
        assertTrue(results.any { it.composableName == "B" })
    }

    @Test
    fun `window evicts oldest sample when full`() {
        // fill window then add a large spike — slope should reflect recent trend
        listOf(1, 1, 1, 1, 1).forEach { analyzer.record("Spike", it) }
        analyzer.record("Spike", 100)
        val result = analyzer.analyze("Spike")
        // After eviction the window is [1,1,1,1,100] — slope should be positive
        assertEquals(RecompositionTrendAnalyzer.TrendDirection.INCREASING, result.direction)
    }

    @Test
    fun `reset clears all history`() {
        listOf(1, 2, 3).forEach { analyzer.record("X", it) }
        analyzer.reset()
        assertTrue(analyzer.analyzeAll().isEmpty())
    }

    @Test
    fun `analyze returns STABLE for unknown composable`() {
        val result = analyzer.analyze("Unknown")
        assertEquals(RecompositionTrendAnalyzer.TrendDirection.STABLE, result.direction)
        assertEquals(0.0, result.slope, 0.001)
    }
}
