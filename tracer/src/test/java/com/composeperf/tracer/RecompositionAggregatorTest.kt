package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionAggregatorTest {

    private lateinit var aggregator: RecompositionAggregator

    @Before
    fun setUp() {
        aggregator = RecompositionAggregator()
    }

    @Test
    fun `aggregate returns zero result for empty map`() {
        val result = aggregator.aggregate(emptyMap())
        assertEquals(0L, result.totalRecompositions)
        assertEquals(0, result.uniqueComposables)
        assertEquals(0L, result.maxRecompositions)
        assertEquals(0L, result.minRecompositions)
        assertEquals(0.0, result.averageRecompositions, 0.0001)
        assertTrue(result.topOffenders.isEmpty())
    }

    @Test
    fun `aggregate computes correct totals`() {
        val counts = mapOf("A" to 10L, "B" to 20L, "C" to 30L)
        val result = aggregator.aggregate(counts)
        assertEquals(60L, result.totalRecompositions)
        assertEquals(3, result.uniqueComposables)
        assertEquals(30L, result.maxRecompositions)
        assertEquals(10L, result.minRecompositions)
        assertEquals(20.0, result.averageRecompositions, 0.0001)
    }

    @Test
    fun `aggregate returns correct top offenders sorted descending`() {
        val counts = mapOf(
            "Alpha" to 5L,
            "Beta" to 50L,
            "Gamma" to 25L,
            "Delta" to 100L,
            "Epsilon" to 10L,
            "Zeta" to 75L
        )
        val result = aggregator.aggregate(counts, topN = 3)
        assertEquals(3, result.topOffenders.size)
        assertEquals("Delta" to 100L, result.topOffenders[0])
        assertEquals("Zeta" to 75L, result.topOffenders[1])
        assertEquals("Beta" to 50L, result.topOffenders[2])
    }

    @Test
    fun `aggregate with single entry`() {
        val counts = mapOf("OnlyOne" to 42L)
        val result = aggregator.aggregate(counts)
        assertEquals(42L, result.totalRecompositions)
        assertEquals(1, result.uniqueComposables)
        assertEquals(42L, result.maxRecompositions)
        assertEquals(42L, result.minRecompositions)
        assertEquals(42.0, result.averageRecompositions, 0.0001)
        assertEquals(1, result.topOffenders.size)
        assertEquals("OnlyOne" to 42L, result.topOffenders[0])
    }

    @Test
    fun `aggregate topN larger than map size returns all entries`() {
        val counts = mapOf("X" to 3L, "Y" to 7L)
        val result = aggregator.aggregate(counts, topN = 10)
        assertEquals(2, result.topOffenders.size)
    }
}
