package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionCorrelationReporterTest {

    private lateinit var analyzer: RecompositionCorrelationAnalyzer
    private lateinit var reporter: RecompositionCorrelationReporter

    @Before
    fun setUp() {
        analyzer = RecompositionCorrelationAnalyzer()
        reporter = RecompositionCorrelationReporter(
            analyzer = analyzer,
            minCoOccurrences = 2,
            topN = 5
        )
    }

    @Test
    fun `buildReport returns no correlations message when empty`() {
        val report = reporter.buildReport()
        assertEquals("No significant correlations found.", report)
    }

    @Test
    fun `buildReport returns no correlations when below minCoOccurrences`() {
        analyzer.recordWindow(setOf("A", "B"))
        val report = reporter.buildReport()
        assertEquals("No significant correlations found.", report)
    }

    @Test
    fun `buildReport contains pair names and count`() {
        repeat(3) { analyzer.recordWindow(setOf("ListItem", "ScrollBar")) }
        val report = reporter.buildReport()

        assertTrue(report.contains("ListItem"))
        assertTrue(report.contains("ScrollBar"))
        assertTrue(report.contains("3x"))
    }

    @Test
    fun `buildReport respects topN limit`() {
        // Create 6 unique pairs each with 3 co-occurrences
        val names = listOf("A", "B", "C", "D")
        for (i in names.indices) {
            for (j in i + 1 until names.size) {
                repeat(3) { analyzer.recordWindow(setOf(names[i], names[j])) }
            }
        }
        // 4 choose 2 = 6 pairs, topN = 5
        val report = reporter.buildReport()
        val lineCount = report.lines().count { it.trimStart().matches(Regex("\\d+\..*")) }
        assertEquals(5, lineCount)
    }

    @Test
    fun `buildReport lists pairs in descending order`() {
        repeat(5) { analyzer.recordWindow(setOf("High", "Freq")) }
        repeat(2) { analyzer.recordWindow(setOf("Low", "Freq")) }

        val report = reporter.buildReport()
        val highIndex = report.indexOf("High")
        val lowIndex = report.indexOf("Low")
        assertTrue("High-Freq pair should appear before Low-Freq pair", highIndex < lowIndex)
    }
}
