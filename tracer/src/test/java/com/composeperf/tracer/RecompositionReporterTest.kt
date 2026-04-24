package com.composeperf.tracer

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionReporterTest {

    private lateinit var registry: RecompositionRegistry
    private lateinit var threshold: RecompositionThreshold
    private lateinit var reporter: RecompositionReporter

    @Before
    fun setUp() {
        registry = mockk()
        threshold = RecompositionThreshold(warningLevel = 2, criticalLevel = 10)
        reporter = RecompositionReporter(registry, threshold)
    }

    @Test
    fun `generateReport filters composables below warning threshold`() {
        every { registry.getAllCounts() } returns mapOf("HighComp" to 5, "LowComp" to 1)
        val report = reporter.generateReport()
        assertEquals(1, report.snapshots.size)
        assertEquals("HighComp", report.snapshots.first().composableName)
    }

    @Test
    fun `generateReport returns empty report when all counts are below threshold`() {
        every { registry.getAllCounts() } returns mapOf("A" to 0, "B" to 1)
        val report = reporter.generateReport()
        assertTrue(report.isEmpty)
    }

    @Test
    fun `format returns no-recompositions message for empty report`() {
        val report = RecompositionReport(snapshots = emptyList())
        val output = reporter.format(report)
        assertTrue(output.contains("No recompositions above threshold"))
    }

    @Test
    fun `format includes total recompositions and hotspot list`() {
        val report = RecompositionReport(
            snapshots = listOf(
                RecompositionSnapshot("ButtonRow", 12),
                RecompositionSnapshot("HeaderCard", 4)
            )
        )
        val output = reporter.format(report)
        assertTrue(output.contains("Total recompositions"))
        assertTrue(output.contains("ButtonRow"))
        assertTrue(output.contains("HeaderCard"))
    }

    @Test
    fun `format marks critical composables with red indicator`() {
        val report = RecompositionReport(
            snapshots = listOf(RecompositionSnapshot("HeavyList", 15))
        )
        val output = reporter.format(report)
        assertTrue(output.contains("🔴"))
        assertFalse(output.contains("🟡"))
    }
}
