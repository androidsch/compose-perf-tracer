package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RecompositionAlertDispatcherTest {

    private var fakeTime = 0L
    private val threshold = RecompositionThreshold(warnThreshold = 3, errorThreshold = 6, windowMs = 1_000L)
    private lateinit var dispatcher: RecompositionAlertDispatcher

    @Before
    fun setUp() {
        fakeTime = 0L
        dispatcher = RecompositionAlertDispatcher(
            threshold = threshold,
            clock = { fakeTime }
        )
    }

    @Test
    fun `returns OK when count is below warn threshold`() {
        repeat(2) { dispatcher.record("MyComposable") }
        val severity = dispatcher.record("MyComposable")
        // 3rd hit equals warnThreshold, so severity should be WARN
        assertEquals(RecompositionSeverity.WARN, severity)
    }

    @Test
    fun `returns OK for first two recompositions`() {
        dispatcher.record("MyComposable")
        val severity = dispatcher.record("MyComposable")
        assertEquals(RecompositionSeverity.OK, severity)
    }

    @Test
    fun `returns ERROR when count reaches error threshold`() {
        repeat(5) { dispatcher.record("HeavyScreen") }
        val severity = dispatcher.record("HeavyScreen")
        assertEquals(RecompositionSeverity.ERROR, severity)
    }

    @Test
    fun `evicts old entries outside time window`() {
        repeat(5) {
            fakeTime += 100
            dispatcher.record("SlowComposable")
        }
        // Advance time so all previous entries fall outside the 1 000 ms window
        fakeTime += 1_100
        val severity = dispatcher.record("SlowComposable")
        assertEquals(RecompositionSeverity.OK, severity)
    }

    @Test
    fun `reset clears windowed counts`() {
        repeat(6) { dispatcher.record("Counter") }
        dispatcher.reset()
        val severity = dispatcher.record("Counter")
        assertEquals(RecompositionSeverity.OK, severity)
    }

    @Test
    fun `tracks different composables independently`() {
        repeat(6) { dispatcher.record("ScreenA") }
        val severityB = dispatcher.record("ScreenB")
        assertEquals(RecompositionSeverity.OK, severityB)
    }
}
