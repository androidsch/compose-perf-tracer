package com.composeperf.tracer

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class RecompositionAnnotationReporterTest {

    private lateinit var processor: RecompositionAnnotationProcessor
    private lateinit var registry: RecompositionRegistry
    private lateinit var reporter: RecompositionAnnotationReporter

    @Before
    fun setUp() {
        processor = mockk(relaxed = true)
        registry = RecompositionRegistry()
        reporter = RecompositionAnnotationReporter(processor, registry)
    }

    @Test
    fun `report does not throw when registry is empty`() {
        reporter.report() // should not throw
    }

    @Test
    fun `report processes all entries from registry`() {
        registry.record("ScreenA")
        registry.record("ScreenA")
        registry.record("ScreenB")

        every { processor.isHighFrequency("ScreenA") } returns false
        every { processor.isHighFrequency("ScreenB") } returns true
        every { processor.getStableLimit("ScreenA") } returns 1
        every { processor.getStableLimit("ScreenB") } returns null

        reporter.report() // should not throw and should log correctly
    }

    @Test
    fun `report marks stable violation correctly`() {
        registry.record("NavBar")
        registry.record("NavBar")
        registry.record("NavBar")

        every { processor.isHighFrequency("NavBar") } returns false
        every { processor.getStableLimit("NavBar") } returns 1

        reporter.report() // should log STABLE-VIOLATION for NavBar
    }

    @Test
    fun `report marks stable ok when within limit`() {
        registry.record("NavBar")

        every { processor.isHighFrequency("NavBar") } returns false
        every { processor.getStableLimit("NavBar") } returns 3

        reporter.report() // should log STABLE-OK for NavBar
    }
}
