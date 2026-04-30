package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionFlowAnalyzerTest {

    private lateinit var analyzer: RecompositionFlowAnalyzer

    @Before
    fun setUp() {
        analyzer = RecompositionFlowAnalyzer()
    }

    @Test
    fun `recordPropagation stores edge between parent and child`() {
        analyzer.recordPropagation("ParentScreen", "ChildCard")
        assertTrue(analyzer.childrenOf("ParentScreen").contains("ChildCard"))
    }

    @Test
    fun `childrenOf returns empty set for unknown composable`() {
        assertTrue(analyzer.childrenOf("Unknown").isEmpty())
    }

    @Test
    fun `topPropagators returns composables sorted by propagation count`() {
        repeat(5) { analyzer.recordPropagation("RootScreen", "Child$it") }
        repeat(2) { analyzer.recordPropagation("SubScreen", "Item$it") }

        val top = analyzer.topPropagators(2)
        assertEquals("RootScreen", top[0].first)
        assertEquals(5, top[0].second)
        assertEquals("SubScreen", top[1].first)
        assertEquals(2, top[1].second)
    }

    @Test
    fun `propagationChain returns full DFS traversal from root`() {
        analyzer.recordPropagation("A", "B")
        analyzer.recordPropagation("B", "C")
        analyzer.recordPropagation("A", "D")

        val chain = analyzer.propagationChain("A")
        assertTrue(chain.contains("A"))
        assertTrue(chain.contains("B"))
        assertTrue(chain.contains("C"))
        assertTrue(chain.contains("D"))
    }

    @Test
    fun `propagationChain handles cycles without infinite loop`() {
        analyzer.recordPropagation("X", "Y")
        analyzer.recordPropagation("Y", "X")

        val chain = analyzer.propagationChain("X")
        assertEquals(2, chain.size)
    }

    @Test
    fun `edgeCount returns total number of unique edges`() {
        analyzer.recordPropagation("A", "B")
        analyzer.recordPropagation("A", "C")
        analyzer.recordPropagation("B", "D")

        assertEquals(3, analyzer.edgeCount())
    }

    @Test
    fun `duplicate edges are not double-counted in edge set`() {
        analyzer.recordPropagation("A", "B")
        analyzer.recordPropagation("A", "B")

        assertEquals(1, analyzer.edgeCount())
    }

    @Test
    fun `reset clears all recorded data`() {
        analyzer.recordPropagation("A", "B")
        analyzer.reset()

        assertTrue(analyzer.childrenOf("A").isEmpty())
        assertTrue(analyzer.topPropagators().isEmpty())
        assertEquals(0, analyzer.edgeCount())
    }
}
