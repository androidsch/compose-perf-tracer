package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionRateAnalyzerTest {

    private lateinit var analyzer: RecompositionRateAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionRateAnalyzer(windowMs = 1000L)
    }

    @Test
    fun `analyze returns empty list when no records`() {
        val result = analyzer.analyze(nowMs = 0L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `analyze aggregates counts within window`() {
        analyzer.record("ButtonA", 3, nowMs = 500L)
        analyzer.record("ButtonA", 2, nowMs = 700L)
        val result = analyzer.analyze(nowMs = 1000L)
        assertEquals(1, result.size)
        assertEquals("ButtonA", result[0].composableName)
        assertEquals(5, result[0].count)
    }

    @Test
    fun `analyze evicts records outside window`() {
        analyzer.record("OldItem", 10, nowMs = 0L)
        analyzer.record("NewItem", 2, nowMs = 1500L)
        val result = analyzer.analyze(nowMs = 1500L)
        assertEquals(1, result.size)
        assertEquals("NewItem", result[0].composableName)
    }

    @Test
    fun `analyze sorts by rps descending`() {
        analyzer.record("Slow", 1, nowMs = 900L)
        analyzer.record("Fast", 20, nowMs = 900L)
        val result = analyzer.analyze(nowMs = 1000L)
        assertEquals("Fast", result[0].composableName)
        assertEquals("Slow", result[1].composableName)
    }

    @Test
    fun `clear removes all history`() {
        analyzer.record("SomeComposable", 5, nowMs = 500L)
        analyzer.clear()
        val result = analyzer.analyze(nowMs = 600L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `rps is computed correctly`() {
        // 10 recompositions in a 1000ms window => 10 rps
        analyzer.record("Counter", 10, nowMs = 500L)
        val result = analyzer.analyze(nowMs = 1000L)
        assertEquals(10.0, result[0].recompositionsPerSecond, 0.01)
    }
}
