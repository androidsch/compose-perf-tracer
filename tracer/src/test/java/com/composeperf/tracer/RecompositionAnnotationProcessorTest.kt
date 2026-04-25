package com.composeperf.tracer

import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionAnnotationProcessorTest {

    private lateinit var alertDispatcher: RecompositionAlertDispatcher
    private lateinit var registry: RecompositionRegistry
    private lateinit var processor: RecompositionAnnotationProcessor

    @Before
    fun setUp() {
        alertDispatcher = mockk(relaxed = true)
        registry = RecompositionRegistry()
        processor = RecompositionAnnotationProcessor(alertDispatcher, registry)
    }

    @Test
    fun `register high frequency composable marks it correctly`() {
        processor.register(
            label = "AnimatedButton",
            highFreqAnnotation = HighFrequencyComposable(reason = "animation")
        )
        assertTrue(processor.isHighFrequency("AnimatedButton"))
        assertFalse(processor.isHighFrequency("StaticText"))
    }

    @Test
    fun `register stable composable stores limit`() {
        processor.register(
            label = "HeaderBar",
            stableAnnotation = StableComposable(maxAllowed = 2)
        )
        assertEquals(2, processor.getStableLimit("HeaderBar"))
        assertNull(processor.getStableLimit("Unknown"))
    }

    @Test
    fun `evaluate dispatches alert when stable limit exceeded`() {
        registry.record("HeaderBar")
        registry.record("HeaderBar")
        registry.record("HeaderBar")

        processor.register(
            label = "HeaderBar",
            stableAnnotation = StableComposable(maxAllowed = 2)
        )

        processor.evaluate()

        verify {
            alertDispatcher.dispatch(
                label = "HeaderBar",
                count = 3,
                message = any()
            )
        }
    }

    @Test
    fun `evaluate does not dispatch alert when within stable limit`() {
        registry.record("HeaderBar")

        processor.register(
            label = "HeaderBar",
            stableAnnotation = StableComposable(maxAllowed = 2)
        )

        processor.evaluate()

        verify(exactly = 0) { alertDispatcher.dispatch(any(), any(), any()) }
    }

    @Test
    fun `evaluate ignores composables without stable annotation`() {
        registry.record("FrequentWidget")
        registry.record("FrequentWidget")
        registry.record("FrequentWidget")

        processor.evaluate()

        verify(exactly = 0) { alertDispatcher.dispatch(any(), any(), any()) }
    }
}
