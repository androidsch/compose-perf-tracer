package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecompositionSpikeDetectorTest {

    private lateinit var detector: RecompositionSpikeDetector

    @Before
    fun setUp() {
        detector = RecompositionSpikeDetector(windowSize = 3, spikeMultiplier = 3.0)
    }

    @Test
    fun `no spike when history is empty`() {
        val result = detector.record("HomeScreen", 10)
        assertNull(result)
    }

    @Test
    fun `no spike when count is below threshold`() {
        detector.record("HomeScreen", 5)
        detector.record("HomeScreen", 5)
        val result = detector.record("HomeScreen", 8)
        assertNull(result)
    }

    @Test
    fun `spike detected when count exceeds average by multiplier`() {
        detector.record("HomeScreen", 4)
        detector.record("HomeScreen", 4)
        val spike = detector.record("HomeScreen", 40)
        assertNotNull(spike)
        assertEquals("HomeScreen", spike!!.composableName)
        assertEquals(40, spike.currentCount)
    }

    @Test
    fun `spike multiplier is calculated correctly`() {
        detector.record("Card", 2)
        detector.record("Card", 2)
        val spike = detector.record("Card", 20)
        assertNotNull(spike)
        assertEquals(10.0, spike!!.multiplier, 0.01)
    }

    @Test
    fun `window slides correctly and evicts oldest sample`() {
        detector.record("Button", 100)
        detector.record("Button", 100)
        detector.record("Button", 100)
        // oldest 100 is evicted; new window is [100, 100, 5] -> avg ~68.3
        val result = detector.record("Button", 5)
        assertNull(result)
        assertEquals(listOf(100, 100, 5), detector.getWindow("Button"))
    }

    @Test
    fun `reset clears all history`() {
        detector.record("Screen", 10)
        detector.reset()
        assertEquals(emptyList<Int>(), detector.getWindow("Screen"))
    }

    @Test
    fun `independent tracking per composable`() {
        detector.record("A", 5)
        detector.record("A", 5)
        detector.record("B", 1)
        detector.record("B", 1)

        val spikeA = detector.record("A", 50)
        val spikeB = detector.record("B", 2)

        assertNotNull(spikeA)
        assertNull(spikeB)
    }
}
