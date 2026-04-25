package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecompositionOutlierDetectorTest {

    private val detector = RecompositionOutlierDetector(zScoreThreshold = 2.0)

    @Test
    fun `returns empty list when fewer than two entries`() {
        assertTrue(detector.detect(emptyMap()).isEmpty())
        assertTrue(detector.detect(mapOf("A" to 5)).isEmpty())
    }

    @Test
    fun `returns empty list when all counts are equal`() {
        val counts = mapOf("A" to 3, "B" to 3, "C" to 3)
        assertTrue(detector.detect(counts).isEmpty())
    }

    @Test
    fun `detects single outlier above threshold`() {
        // A, B, C have low counts; D is a clear outlier
        val counts = mapOf(
            "A" to 2,
            "B" to 3,
            "C" to 2,
            "D" to 50
        )
        val outliers = detector.detect(counts)
        assertEquals(1, outliers.size)
        assertEquals("D", outliers.first().composableName)
        assertEquals(50, outliers.first().count)
        assertTrue(outliers.first().zScore >= 2.0)
    }

    @Test
    fun `results are sorted by z-score descending`() {
        val counts = mapOf(
            "A" to 1,
            "B" to 2,
            "C" to 100,
            "D" to 80
        )
        val outliers = detector.detect(counts)
        assertTrue(outliers.size >= 2)
        assertTrue(outliers[0].zScore >= outliers[1].zScore)
    }

    @Test
    fun `custom z-score threshold filters results`() {
        val counts = mapOf(
            "A" to 1,
            "B" to 2,
            "C" to 3,
            "D" to 20
        )
        val strictDetector = RecompositionOutlierDetector(zScoreThreshold = 3.0)
        val lenientDetector = RecompositionOutlierDetector(zScoreThreshold = 1.0)

        val strictOutliers = strictDetector.detect(counts)
        val lenientOutliers = lenientDetector.detect(counts)

        assertTrue(lenientOutliers.size >= strictOutliers.size)
    }
}
