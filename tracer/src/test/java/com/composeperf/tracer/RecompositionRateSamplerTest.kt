package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionRateSamplerTest {

    private lateinit var registry: RecompositionRegistry
    private lateinit var analyzer: RecompositionRateAnalyzer
    private lateinit var sampler: RecompositionRateSampler

    @Before
    fun setUp() {
        registry = RecompositionRegistry()
        analyzer = RecompositionRateAnalyzer(windowMs = 2000L)
        sampler = RecompositionRateSampler(registry, analyzer)
    }

    @Test
    fun `sample records delta counts into analyzer`() {
        registry.increment("CardItem")
        registry.increment("CardItem")
        registry.increment("CardItem")

        sampler.sample(nowMs = 500L)

        val results = analyzer.analyze(nowMs = 1000L)
        assertEquals(1, results.size)
        assertEquals("CardItem", results[0].composableName)
        assertEquals(3, results[0].count)
    }

    @Test
    fun `sample only records delta since last sample`() {
        registry.increment("Header")
        registry.increment("Header")
        sampler.sample(nowMs = 200L)

        registry.increment("Header")
        sampler.sample(nowMs = 400L)

        val results = analyzer.analyze(nowMs = 600L)
        val entry = results.first { it.composableName == "Header" }
        // Total delta = 2 + 1 = 3
        assertEquals(3, entry.count)
    }

    @Test
    fun `reset clears previous counts and analyzer`() {
        registry.increment("Footer")
        sampler.sample(nowMs = 100L)
        sampler.reset()

        val results = analyzer.analyze(nowMs = 200L)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `sample ignores zero deltas`() {
        registry.increment("Static")
        sampler.sample(nowMs = 100L)
        // No new increments
        sampler.sample(nowMs = 200L)

        val results = analyzer.analyze(nowMs = 300L)
        val entry = results.first { it.composableName == "Static" }
        // Only the first sample (delta=1) should be recorded
        assertEquals(1, entry.count)
    }
}
