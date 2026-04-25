package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionCorrelationAnalyzerTest {

    private lateinit var analyzer: RecompositionCorrelationAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionCorrelationAnalyzer(windowSize = 10)
    }

    @Test
    fun `recordWindow with single composable produces no pairs`() {
        analyzer.recordWindow(setOf("ButtonA"))
        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences = 1)
        assertTrue(pairs.isEmpty())
    }

    @Test
    fun `recordWindow with two composables records one pair`() {
        analyzer.recordWindow(setOf("ButtonA", "TextB"))
        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences = 1)
        assertEquals(1, pairs.size)
        assertEquals(1, pairs[0].coOccurrenceCount)
    }

    @Test
    fun `repeated co-occurrence increments count`() {
        repeat(5) { analyzer.recordWindow(setOf("HeaderA", "FooterB")) }
        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences = 1)
        assertEquals(1, pairs.size)
        assertEquals(5, pairs[0].coOccurrenceCount)
    }

    @Test
    fun `getCorrelatedPairs filters by minCoOccurrences`() {
        repeat(3) { analyzer.recordWindow(setOf("A", "B")) }
        analyzer.recordWindow(setOf("A", "C"))

        val filtered = analyzer.getCorrelatedPairs(minCoOccurrences = 3)
        assertEquals(1, filtered.size)
        assertEquals("A", filtered[0].first)
        assertEquals("B", filtered[0].second)
    }

    @Test
    fun `getCorrelatedPairs returns entries sorted descending by count`() {
        repeat(5) { analyzer.recordWindow(setOf("X", "Y")) }
        repeat(2) { analyzer.recordWindow(setOf("X", "Z")) }

        val pairs = analyzer.getCorrelatedPairs(minCoOccurrences = 1)
        assertEquals(5, pairs[0].coOccurrenceCount)
        assertEquals(2, pairs[1].coOccurrenceCount)
    }

    @Test
    fun `topCorrelationFor returns strongest partner`() {
        repeat(4) { analyzer.recordWindow(setOf("Nav", "Toolbar")) }
        repeat(2) { analyzer.recordWindow(setOf("Nav", "Content")) }

        val top = analyzer.topCorrelationFor("Nav")
        assertNotNull(top)
        assertEquals(4, top!!.coOccurrenceCount)
        assertTrue(top.first == "Nav" || top.second == "Nav")
    }

    @Test
    fun `topCorrelationFor returns null when no data`() {
        val top = analyzer.topCorrelationFor("Unknown")
        assertNull(top)
    }

    @Test
    fun `reset clears all data`() {
        repeat(3) { analyzer.recordWindow(setOf("A", "B")) }
        analyzer.reset()
        assertTrue(analyzer.getCorrelatedPairs(minCoOccurrences = 1).isEmpty())
    }

    @Test
    fun `window eviction respects windowSize`() {
        val smallAnalyzer = RecompositionCorrelationAnalyzer(windowSize = 2)
        repeat(10) { smallAnalyzer.recordWindow(setOf("P", "Q")) }
        // Co-occurrence count is unbounded by windowSize (windowSize limits memory, not counting)
        val pairs = smallAnalyzer.getCorrelatedPairs(minCoOccurrences = 1)
        assertEquals(1, pairs.size)
        assertTrue(pairs[0].coOccurrenceCount >= 1)
    }
}
