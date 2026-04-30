package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecompositionPatternDetectorTest {

    private lateinit var detector: RecompositionPatternDetector

    @Before
    fun setUp() {
        detector = RecompositionPatternDetector(minOccurrences = 2, windowSize = 10)
    }

    @Test
    fun `detectPatterns returns empty when no batches recorded`() {
        val patterns = detector.detectPatterns()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `detectPatterns returns empty when occurrences below threshold`() {
        detector.recordBatch(listOf("CompA", "CompB"))
        val patterns = detector.detectPatterns()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `detectPatterns returns pattern when threshold met`() {
        repeat(2) { detector.recordBatch(listOf("CompA", "CompB")) }
        val patterns = detector.detectPatterns()
        assertEquals(1, patterns.size)
        assertEquals(listOf("CompA", "CompB"), patterns[0].composables)
        assertEquals(2, patterns[0].occurrences)
    }

    @Test
    fun `detectPatterns sorts composables within batch`() {
        detector.recordBatch(listOf("CompZ", "CompA"))
        detector.recordBatch(listOf("CompA", "CompZ"))
        val patterns = detector.detectPatterns()
        assertEquals(1, patterns.size)
        assertEquals(listOf("CompA", "CompZ"), patterns[0].composables)
        assertEquals(2, patterns[0].occurrences)
    }

    @Test
    fun `detectPatterns respects window size`() {
        // Fill window with one pattern
        repeat(10) { detector.recordBatch(listOf("CompOld")) }
        // Push old entries out
        repeat(10) { detector.recordBatch(listOf("CompNew")) }
        val patterns = detector.detectPatterns()
        val composableNames = patterns.flatMap { it.composables }
        assertTrue("CompOld" !in composableNames)
        assertTrue("CompNew" in composableNames)
    }

    @Test
    fun `reset clears all tracked windows`() {
        repeat(3) { detector.recordBatch(listOf("CompA")) }
        detector.reset()
        val patterns = detector.detectPatterns()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `detectPatterns returns results sorted by occurrences descending`() {
        repeat(3) { detector.recordBatch(listOf("CompA")) }
        repeat(2) { detector.recordBatch(listOf("CompB")) }
        val patterns = detector.detectPatterns()
        assertEquals(2, patterns.size)
        assertTrue(patterns[0].occurrences >= patterns[1].occurrences)
    }
}
