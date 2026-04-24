package com.composeperf.tracer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecompositionFilterTest {

    // ── PassThroughFilter ────────────────────────────────────────────────────

    @Test
    fun `PassThroughFilter always includes every composable`() {
        assertTrue(PassThroughFilter.shouldInclude("AnyComposable", 1))
        assertTrue(PassThroughFilter.shouldInclude("AnotherComposable", 100))
    }

    // ── PrefixExclusionFilter ────────────────────────────────────────────────

    @Test
    fun `PrefixExclusionFilter excludes composable matching a prefix`() {
        val filter = PrefixExclusionFilter(setOf("Internal", "Debug"))
        assertFalse(filter.shouldInclude("InternalHeader", 5))
        assertFalse(filter.shouldInclude("DebugOverlay", 3))
    }

    @Test
    fun `PrefixExclusionFilter includes composable not matching any prefix`() {
        val filter = PrefixExclusionFilter(setOf("Internal", "Debug"))
        assertTrue(filter.shouldInclude("HomeScreen", 2))
        assertTrue(filter.shouldInclude("ProfileCard", 10))
    }

    @Test
    fun `PrefixExclusionFilter with empty set includes everything`() {
        val filter = PrefixExclusionFilter(emptySet())
        assertTrue(filter.shouldInclude("AnyComposable", 7))
    }

    // ── MinCountFilter ───────────────────────────────────────────────────────

    @Test
    fun `MinCountFilter includes composable meeting minimum count`() {
        val filter = MinCountFilter(3)
        assertTrue(filter.shouldInclude("Card", 3))
        assertTrue(filter.shouldInclude("Card", 10))
    }

    @Test
    fun `MinCountFilter excludes composable below minimum count`() {
        val filter = MinCountFilter(3)
        assertFalse(filter.shouldInclude("Card", 1))
        assertFalse(filter.shouldInclude("Card", 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MinCountFilter throws when minimumCount is zero`() {
        MinCountFilter(0)
    }

    // ── CompositeFilter ──────────────────────────────────────────────────────

    @Test
    fun `CompositeFilter includes only when all filters pass`() {
        val filter = CompositeFilter.of(
            PrefixExclusionFilter(setOf("Debug")),
            MinCountFilter(2)
        )
        // passes both: not excluded, count >= 2
        assertTrue(filter.shouldInclude("HomeScreen", 4))
    }

    @Test
    fun `CompositeFilter excludes when prefix filter rejects`() {
        val filter = CompositeFilter.of(
            PrefixExclusionFilter(setOf("Debug")),
            MinCountFilter(2)
        )
        assertFalse(filter.shouldInclude("DebugOverlay", 5))
    }

    @Test
    fun `CompositeFilter excludes when count filter rejects`() {
        val filter = CompositeFilter.of(
            PrefixExclusionFilter(setOf("Debug")),
            MinCountFilter(3)
        )
        assertFalse(filter.shouldInclude("HomeScreen", 1))
    }

    @Test
    fun `CompositeFilter with empty list behaves as pass-through`() {
        val filter = CompositeFilter(emptyList())
        assertTrue(filter.shouldInclude("AnyComposable", 1))
    }
}
