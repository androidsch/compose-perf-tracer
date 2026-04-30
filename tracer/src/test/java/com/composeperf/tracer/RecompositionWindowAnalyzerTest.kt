package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionWindowAnalyzerTest {

    private var fakeTime = 0L
    private lateinit var analyzer: RecompositionWindowAnalyzer

    @Before
    fun setUp() {
        fakeTime = 0L
        analyzer = RecompositionWindowAnalyzer(windowSizeMs = 1000L, clock = { fakeTime })
    }

    @Test
    fun `summarize returns empty when no entries recorded`() {
        assertTrue(analyzer.summarize().isEmpty())
    }

    @Test
    fun `summarize aggregates counts for same composable`() {
        analyzer.record("ButtonA", 3)
        analyzer.record("ButtonA", 5)
        val summaries = analyzer.summarize()
        assertEquals(1, summaries.size)
        assertEquals(8, summaries[0].totalCount)
        assertEquals(2, summaries[0].entriesInWindow)
    }

    @Test
    fun `summarize prunes entries outside window`() {
        fakeTime = 0L
        analyzer.record("OldComponent", 10)
        fakeTime = 1500L
        analyzer.record("NewComponent", 2)
        val summaries = analyzer.summarize()
        assertEquals(1, summaries.size)
        assertEquals("NewComponent", summaries[0].composableName)
    }

    @Test
    fun `summarize sorts by totalCount descending`() {
        analyzer.record("LowCount", 1)
        analyzer.record("HighCount", 20)
        analyzer.record("MidCount", 5)
        val summaries = analyzer.summarize()
        assertEquals("HighCount", summaries[0].composableName)
        assertEquals("MidCount", summaries[1].composableName)
        assertEquals("LowCount", summaries[2].composableName)
    }

    @Test
    fun `topN returns at most N entries`() {
        repeat(5) { i -> analyzer.record("Component$i", i + 1) }
        val top2 = analyzer.topN(2)
        assertEquals(2, top2.size)
    }

    @Test
    fun `topN with zero returns empty list`() {
        analyzer.record("SomeComponent", 5)
        assertTrue(analyzer.topN(0).isEmpty())
    }

    @Test
    fun `reset clears all entries`() {
        analyzer.record("ButtonA", 3)
        analyzer.reset()
        assertTrue(analyzer.summarize().isEmpty())
    }

    @Test
    fun `entries exactly at window boundary are retained`() {
        fakeTime = 0L
        analyzer.record("BorderComponent", 4)
        fakeTime = 1000L
        val summaries = analyzer.summarize()
        assertEquals(1, summaries.size)
    }
}
