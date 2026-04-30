package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionChainAnalyzerTest {

    private lateinit var analyzer: RecompositionChainAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionChainAnalyzer(minChainLength = 2, minTotalRecompositions = 3)
    }

    private fun snapshot(name: String, count: Int) =
        RecompositionSnapshot(name, count)

    @Test
    fun `returns empty list when no snapshots provided`() {
        val result = analyzer.analyze(emptyMap(), emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detects simple parent-child chain`() {
        val snapshots = mapOf(
            "ParentA" to snapshot("ParentA", 5),
            "ChildB" to snapshot("ChildB", 4)
        )
        val parentMap = mapOf("ChildB" to "ParentA")
        val result = analyzer.analyze(snapshots, parentMap)
        assertEquals(1, result.size)
        assertEquals("ParentA", result[0].rootComposable)
        assertEquals(listOf("ParentA", "ChildB"), result[0].chain)
        assertEquals(2, result[0].depth)
        assertEquals(9, result[0].totalRecompositions)
    }

    @Test
    fun `ignores chain with zero recompositions`() {
        val snapshots = mapOf(
            "Root" to snapshot("Root", 0),
            "Child" to snapshot("Child", 0)
        )
        val parentMap = mapOf("Child" to "Root")
        val result = analyzer.analyze(snapshots, parentMap)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filters chains below minChainLength`() {
        val snapshots = mapOf(
            "Solo" to snapshot("Solo", 10)
        )
        val result = analyzer.analyze(snapshots, emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filters chains below minTotalRecompositions`() {
        val snapshots = mapOf(
            "A" to snapshot("A", 1),
            "B" to snapshot("B", 1)
        )
        val parentMap = mapOf("B" to "A")
        val result = analyzer.analyze(snapshots, parentMap)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `sorts chains by depth descending`() {
        val snapshots = mapOf(
            "R1" to snapshot("R1", 3),
            "C1" to snapshot("C1", 3),
            "R2" to snapshot("R2", 2),
            "C2" to snapshot("C2", 2),
            "C3" to snapshot("C3", 2)
        )
        val parentMap = mapOf(
            "C1" to "R1",
            "C2" to "R2",
            "C3" to "C2"
        )
        val result = analyzer.analyze(snapshots, parentMap)
        assertTrue(result.isNotEmpty())
        assertTrue(result[0].depth >= result.last().depth)
    }

    @Test
    fun `chain depth matches number of composables in chain`() {
        val snapshots = mapOf(
            "A" to snapshot("A", 4),
            "B" to snapshot("B", 3),
            "C" to snapshot("C", 2)
        )
        val parentMap = mapOf("B" to "A", "C" to "B")
        val result = analyzer.analyze(snapshots, parentMap)
        assertEquals(1, result.size)
        assertEquals(3, result[0].depth)
        assertEquals(listOf("A", "B", "C"), result[0].chain)
    }
}
