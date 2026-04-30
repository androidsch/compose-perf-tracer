package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionBurstDetectorTest {

    private var fakeTime = 0L
    private lateinit var detector: RecompositionBurstDetector

    @Before
    fun setUp() {
        fakeTime = 0L
        detector = RecompositionBurstDetector(
            windowMs = 500L,
            burstThreshold = 3,
            clock = { fakeTime }
        )
    }

    @Test
    fun `returns null when recompositions are below threshold`() {
        repeat(2) {
            fakeTime += 50
            assertNull(detector.record("MyComposable"))
        }
    }

    @Test
    fun `returns burst when threshold is reached within window`() {
        repeat(3) {
            fakeTime += 50
            val result = detector.record("MyComposable")
            if (it < 2) assertNull(result) else assertNotNull(result)
        }
    }

    @Test
    fun `burst has correct composable name and count`() {
        repeat(3) { fakeTime += 50; detector.record("BurstComp") }
        fakeTime += 50
        val burst = detector.record("BurstComp")
        assertNotNull(burst)
        assertEquals("BurstComp", burst!!.composableName)
        assertEquals(4, burst.count)
    }

    @Test
    fun `evicts events outside the time window`() {
        fakeTime = 0
        detector.record("SlowComp")
        fakeTime = 600 // outside 500ms window
        detector.record("SlowComp")
        fakeTime = 700
        val result = detector.record("SlowComp")
        // Only 2 events in window — below threshold of 3
        assertNull(result)
    }

    @Test
    fun `activeBursts returns composables currently in burst`() {
        repeat(3) { fakeTime += 50; detector.record("ActiveBurst") }
        val bursts = detector.activeBursts()
        assertEquals(1, bursts.size)
        assertEquals("ActiveBurst", bursts.first().composableName)
    }

    @Test
    fun `activeBursts excludes composables below threshold`() {
        repeat(2) { fakeTime += 50; detector.record("LowComp") }
        assertTrue(detector.activeBursts().isEmpty())
    }

    @Test
    fun `reset clears all state`() {
        repeat(3) { fakeTime += 50; detector.record("ResetComp") }
        detector.reset()
        assertTrue(detector.activeBursts().isEmpty())
        assertNull(detector.record("ResetComp"))
    }

    @Test
    fun `burst rate is positive when duration is non-zero`() {
        fakeTime = 0
        detector.record("RateComp")
        fakeTime = 100
        detector.record("RateComp")
        fakeTime = 200
        val burst = detector.record("RateComp")
        assertNotNull(burst)
        assertTrue(burst!!.rate > 0.0)
    }
}
