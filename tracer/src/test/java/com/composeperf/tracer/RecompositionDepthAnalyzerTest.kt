package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionDepthAnalyzerTest {

    private lateinit var analyzer: RecompositionDepthAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionDepthAnalyzer()
    }

    @Test
    fun `record single entry and verify average depth`() {
        analyzer.record("HomeScreen", depth = 3)
        val averages = analyzer.averageDepths()
        assertEquals(3.0, averages["HomeScreen"]!!, 0.001)
    }

    @Test
    fun `record multiple depths for same composable averages correctly`() {
        analyzer.record("ProfileCard", depth = 2)
        analyzer.record("ProfileCard", depth = 4)
        analyzer.record("ProfileCard", depth = 6)
        val averages = analyzer.averageDepths()
        assertEquals(4.0, averages["ProfileCard"]!!, 0.001)
    }

    @Test
    fun `deepRecompositions returns only entries above threshold`() {
        analyzer.record("ShallowItem", depth = 1)
        analyzer.record("ShallowItem", depth = 2)
        analyzer.record("DeepWidget", depth = 8)
        analyzer.record("DeepWidget", depth = 10)

        val deep = analyzer.deepRecompositions(threshold = 5)
        assertEquals(1, deep.size)
        assertEquals("DeepWidget", deep.first().composableName)
    }

    @Test
    fun `deepRecompositions returns empty list when nothing exceeds threshold`() {
        analyzer.record("ButtonA", depth = 1)
        analyzer.record("ButtonB", depth = 2)
        val deep = analyzer.deepRecompositions(threshold = 10)
        assertTrue(deep.isEmpty())
    }

    @Test
    fun `maxDepth returns highest recorded depth`() {
        analyzer.record("Alpha", depth = 3)
        analyzer.record("Beta", depth = 7)
        analyzer.record("Gamma", depth = 5)
        assertEquals(7, analyzer.maxDepth())
    }

    @Test
    fun `maxDepth returns zero when no entries recorded`() {
        assertEquals(0, analyzer.maxDepth())
    }

    @Test
    fun `reset clears all data`() {
        analyzer.record("SomeComposable", depth = 4)
        analyzer.reset()
        assertEquals(0, analyzer.trackedCount())
        assertEquals(0, analyzer.maxDepth())
        assertTrue(analyzer.averageDepths().isEmpty())
    }

    @Test
    fun `trackedCount reflects number of unique composables`() {
        analyzer.record("A", depth = 1)
        analyzer.record("B", depth = 2)
        analyzer.record("A", depth = 3)
        assertEquals(2, analyzer.trackedCount())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `record throws for negative depth`() {
        analyzer.record("BadComposable", depth = -1)
    }

    @Test
    fun `deepRecompositions entries include correct count`() {
        repeat(5) { analyzer.record("FrequentDeep", depth = 9) }
        val entries = analyzer.deepRecompositions(threshold = 5)
        assertEquals(5, entries.first().count)
    }
}
