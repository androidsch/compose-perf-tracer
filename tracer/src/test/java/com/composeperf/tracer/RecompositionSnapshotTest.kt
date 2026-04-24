package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecompositionSnapshotTest {

    @Test
    fun `snapshot isAboveThreshold is false when count is zero`() {
        val snapshot = RecompositionSnapshot(composableName = "MyComposable", recompositionCount = 0)
        assertFalse(snapshot.isAboveThreshold)
    }

    @Test
    fun `snapshot isAboveThreshold is true when count is positive`() {
        val snapshot = RecompositionSnapshot(composableName = "MyComposable", recompositionCount = 3)
        assertTrue(snapshot.isAboveThreshold)
    }

    @Test
    fun `report totalRecompositions sums all snapshot counts`() {
        val report = RecompositionReport(
            snapshots = listOf(
                RecompositionSnapshot("A", 5),
                RecompositionSnapshot("B", 10),
                RecompositionSnapshot("C", 2)
            )
        )
        assertEquals(17, report.totalRecompositions)
    }

    @Test
    fun `report hotspots are sorted descending by recomposition count`() {
        val report = RecompositionReport(
            snapshots = listOf(
                RecompositionSnapshot("A", 3),
                RecompositionSnapshot("B", 15),
                RecompositionSnapshot("C", 7)
            )
        )
        assertEquals(listOf("B", "C", "A"), report.hotspots.map { it.composableName })
    }

    @Test
    fun `report topN returns at most N entries`() {
        val report = RecompositionReport(
            snapshots = (1..10).map { RecompositionSnapshot("Comp$it", it) }
        )
        assertEquals(3, report.topN(3).size)
    }

    @Test
    fun `empty report isEmpty is true`() {
        val report = RecompositionReport(snapshots = emptyList())
        assertTrue(report.isEmpty)
    }
}
