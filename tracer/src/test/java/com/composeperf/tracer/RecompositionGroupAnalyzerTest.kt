package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionGroupAnalyzerTest {

    private lateinit var analyzer: RecompositionGroupAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionGroupAnalyzer()
    }

    @Test
    fun `group returns empty map for empty input`() {
        val result = analyzer.group(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `group uses first dot-separated segment as key`() {
        val snapshots = listOf(
            RecompositionSnapshot("Home.Header", 3),
            RecompositionSnapshot("Home.Body", 5),
            RecompositionSnapshot("Profile.Avatar", 2)
        )
        val result = analyzer.group(snapshots)
        assertEquals(2, result.size)
        assertEquals(2, result["Home"]?.size)
        assertEquals(1, result["Profile"]?.size)
    }

    @Test
    fun `group uses full name when no dot present`() {
        val snapshots = listOf(
            RecompositionSnapshot("Toolbar", 4),
            RecompositionSnapshot("Toolbar", 1)
        )
        val result = analyzer.group(snapshots)
        assertEquals(1, result.size)
        assertEquals(2, result["Toolbar"]?.size)
    }

    @Test
    fun `summarize returns total count per group`() {
        val snapshots = listOf(
            RecompositionSnapshot("Home.Header", 3),
            RecompositionSnapshot("Home.Body", 5),
            RecompositionSnapshot("Profile.Avatar", 2)
        )
        val result = analyzer.summarize(snapshots)
        assertEquals(8, result["Home"])
        assertEquals(2, result["Profile"])
    }

    @Test
    fun `summarize returns empty map for empty input`() {
        val result = analyzer.summarize(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `topGroups returns sorted groups by count`() {
        val snapshots = listOf(
            RecompositionSnapshot("A.X", 1),
            RecompositionSnapshot("B.X", 10),
            RecompositionSnapshot("C.X", 5),
            RecompositionSnapshot("D.X", 3)
        )
        val top = analyzer.topGroups(snapshots, topN = 2)
        assertEquals(2, top.size)
        assertEquals("B", top[0].first)
        assertEquals(10, top[0].second)
        assertEquals("C", top[1].first)
        assertEquals(5, top[1].second)
    }

    @Test
    fun `topGroups respects topN limit`() {
        val snapshots = (1..10).map { RecompositionSnapshot("Group$it.Item", it) }
        val top = analyzer.topGroups(snapshots, topN = 3)
        assertEquals(3, top.size)
    }

    @Test
    fun `topGroups returns all groups when topN exceeds group count`() {
        val snapshots = listOf(
            RecompositionSnapshot("A.X", 1),
            RecompositionSnapshot("B.X", 2)
        )
        val top = analyzer.topGroups(snapshots, topN = 10)
        assertEquals(2, top.size)
    }

    @Test
    fun `custom groupKeyExtractor is applied`() {
        val customAnalyzer = RecompositionGroupAnalyzer { name -> name.takeLast(3) }
        val snapshots = listOf(
            RecompositionSnapshot("ScreenABC", 2),
            RecompositionSnapshot("WidgetABC", 4)
        )
        val result = customAnalyzer.summarize(snapshots)
        assertEquals(1, result.size)
        assertEquals(6, result["ABC"])
    }
}
