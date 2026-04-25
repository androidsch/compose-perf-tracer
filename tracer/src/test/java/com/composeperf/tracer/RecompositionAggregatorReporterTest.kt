package com.composeperf.tracer

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import android.util.Log
import org.junit.Before
import org.junit.Test

class RecompositionAggregatorReporterTest {

    private lateinit var reporter: RecompositionAggregatorReporter
    private val tag = "RecompositionAggregator"

    @Before
    fun setUp() {
        reporter = RecompositionAggregatorReporter(tag)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @Test
    fun `report logs no data message when result is empty`() {
        val emptyResult = RecompositionAggregator.AggregationResult(
            totalRecompositions = 0L,
            uniqueComposables = 0,
            maxRecompositions = 0L,
            minRecompositions = 0L,
            averageRecompositions = 0.0,
            topOffenders = emptyList()
        )
        reporter.report(emptyResult)
        verify { Log.d(tag, "No recomposition data to report.") }
    }

    @Test
    fun `report logs summary lines for non-empty result`() {
        val result = RecompositionAggregator.AggregationResult(
            totalRecompositions = 60L,
            uniqueComposables = 3,
            maxRecompositions = 30L,
            minRecompositions = 10L,
            averageRecompositions = 20.0,
            topOffenders = listOf("C" to 30L, "B" to 20L, "A" to 10L)
        )
        reporter.report(result)
        verify { Log.d(tag, "=== Recomposition Aggregation Report ===") }
        verify { Log.d(tag, "  Unique composables : 3") }
        verify { Log.d(tag, "  Total recompositions: 60") }
        verify { Log.d(tag, "  Top offenders:") }
        verify { Log.d(tag, "    1. C -> 30") }
    }
}
