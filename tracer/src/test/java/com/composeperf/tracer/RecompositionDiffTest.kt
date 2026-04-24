package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Test

class RecompositionDiffTest {

    private fun snapshot(vararg pairs: Pair<String, Int>) =
        RecompositionSnapshot(counts = mapOf(*pairs))

    @Test
    fun `calculate returns empty list when snapshots are identical`() {
        val snap = snapshot("ButtonA" to 3, "TextB" to 5)
        val diffs = RecompositionDiffCalculator.calculate(snap, snap)
        assertTrue(diffs.isEmpty())
    }

    @Test
    fun `calculate detects increased recomposition`() {
        val prev = snapshot("ButtonA" to 2)
        val curr = snapshot("ButtonA" to 5)
        val diffs = RecompositionDiffCalculator.calculate(prev, curr)
        assertEquals(1, diffs.size)
        assertEquals(3, diffs[0].delta)
        assertFalse(diffs[0].isNew)
        assertFalse(diffs[0].isRemoved)
    }

    @Test
    fun `calculate detects new composable`() {
        val prev = snapshot()
        val curr = snapshot("NewCard" to 4)
        val diffs = RecompositionDiffCalculator.calculate(prev, curr)
        assertEquals(1, diffs.size)
        assertTrue(diffs[0].isNew)
        assertEquals(4, diffs[0].delta)
    }

    @Test
    fun `calculate detects removed composable`() {
        val prev = snapshot("OldItem" to 3)
        val curr = snapshot()
        val diffs = RecompositionDiffCalculator.calculate(prev, curr)
        assertEquals(1, diffs.size)
        assertTrue(diffs[0].isRemoved)
        assertEquals(-3, diffs[0].delta)
    }

    @Test
    fun `onlyIncreased filters out decreased and removed entries`() {
        val prev = snapshot("A" to 5, "B" to 2, "C" to 1)
        val curr = snapshot("A" to 8, "B" to 1)
        val diffs = RecompositionDiffCalculator.onlyIncreased(prev, curr)
        assertEquals(1, diffs.size)
        assertEquals("A", diffs[0].composableName)
        assertEquals(3, diffs[0].delta)
    }

    @Test
    fun `calculate sorts results by delta descending`() {
        val prev = snapshot("A" to 1, "B" to 1, "C" to 1)
        val curr = snapshot("A" to 4, "B" to 10, "C" to 3)
        val diffs = RecompositionDiffCalculator.calculate(prev, curr)
        assertEquals("B", diffs[0].composableName)
        assertEquals("A", diffs[1].composableName)
        assertEquals("C", diffs[2].composableName)
    }

    @Test
    fun `RecompositionDiff toString is descriptive`() {
        val diff = RecompositionDiff("MyComposable", 2, 7)
        val str = diff.toString()
        assertTrue(str.contains("MyComposable"))
        assertTrue(str.contains("delta=5"))
    }
}
