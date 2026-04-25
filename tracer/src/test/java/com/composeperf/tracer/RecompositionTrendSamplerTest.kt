package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class RecompositionTrendSamplerTest {

    private lateinit var registry: RecompositionRegistry
    private lateinit var analyzer: RecompositionTrendAnalyzer
    private lateinit var sampler: RecompositionTrendSampler

    @Before
    fun setUp() {
        registry = mock()
        analyzer = mock()
        sampler = RecompositionTrendSampler(registry, analyzer)
    }

    @Test
    fun `sample increments sampleCount`() {
        whenever(registry.snapshot()).thenReturn(emptyMap())
        sampler.sample()
        sampler.sample()
        assertEquals(2, sampler.getSampleCount())
    }

    @Test
    fun `sample records each composable count into analyzer`() {
        val fakeSnapshot = mapOf("ScreenA" to 4, "ScreenB" to 7)
        whenever(registry.snapshot()).thenReturn(fakeSnapshot)
        sampler.sample()
        verify(analyzer).record("ScreenA", 4)
        verify(analyzer).record("ScreenB", 7)
    }

    @Test
    fun `reset zeroes sampleCount and resets analyzer`() {
        whenever(registry.snapshot()).thenReturn(emptyMap())
        sampler.sample()
        sampler.reset()
        assertEquals(0, sampler.getSampleCount())
        verify(analyzer).reset()
    }

    @Test
    fun `sample with empty snapshot does not call analyzer record`() {
        whenever(registry.snapshot()).thenReturn(emptyMap())
        sampler.sample()
        verify(analyzer, never()).record(any(), any())
    }
}
