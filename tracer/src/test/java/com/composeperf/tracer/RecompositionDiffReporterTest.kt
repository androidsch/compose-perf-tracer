package com.composeperf.tracer

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionDiffReporterTest {

    private lateinit var logger: RecompositionLogger
    private lateinit var reporter: RecompositionDiffReporter

    private fun snapshot(vararg pairs: Pair<String, Int>) =
        RecompositionSnapshot(counts = mapOf(*pairs))

    @Before
    fun setUp() {
        logger = mockk(relaxed = true)
        reporter = RecompositionDiffReporter(logger)
    }

    @Test
    fun `report logs no changes message when snapshots are equal`() {
        val snap = snapshot("A" to 3)
        reporter.report(snap, snap)
        val messageSlot = slot<String>()
        verify { logger.log(any(), capture(messageSlot)) }
        assertTrue(messageSlot.captured.contains("No recomposition changes"))
    }

    @Test
    fun `report logs diff header and entries when changes exist`() {
        val prev = snapshot("CardView" to 1)
        val curr = snapshot("CardView" to 4, "HeaderText" to 2)
        reporter.report(prev, curr, tag = "TestTag")
        verify(atLeast = 2) { logger.log(eq("TestTag"), any()) }
    }

    @Test
    fun `reportIncreased logs no increases message when no growth`() {
        val prev = snapshot("A" to 5)
        val curr = snapshot("A" to 3)
        reporter.reportIncreased(prev, curr)
        val messageSlot = slot<String>()
        verify { logger.log(any(), capture(messageSlot)) }
        assertTrue(messageSlot.captured.contains("No recomposition increases"))
    }

    @Test
    fun `reportIncreased logs only increased composables`() {
        val prev = snapshot("A" to 1, "B" to 5)
        val curr = snapshot("A" to 6, "B" to 3)
        reporter.reportIncreased(prev, curr)
        // Should log header + 1 entry for A only
        verify(exactly = 2) { logger.log(any(), any()) }
    }
}
