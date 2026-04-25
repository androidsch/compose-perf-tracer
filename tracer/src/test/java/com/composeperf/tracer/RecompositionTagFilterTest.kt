package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecompositionTagFilterTest {

    private val tagLookup: Map<String, Set<String>> = mapOf(
        "HomeScreen"    to setOf("screen", "critical"),
        "ProfileCard"   to setOf("card"),
        "SettingsItem"  to setOf("screen", "low-priority"),
        "LoadingSpinner" to setOf("animation")
    )

    private fun lookup(name: String) = tagLookup[name] ?: emptySet()

    // ── shouldInclude ──────────────────────────────────────────────────────────

    @Test
    fun `PASS_ALL includes everything`() {
        val filter = RecompositionTagFilter.PASS_ALL
        assertTrue(filter.shouldInclude("HomeScreen", setOf("screen")))
        assertTrue(filter.shouldInclude("Any", emptySet()))
    }

    @Test
    fun `includedTags restricts to matching composables`() {
        val filter = RecompositionTagFilter(includedTags = setOf("screen"))
        assertTrue(filter.shouldInclude("HomeScreen", setOf("screen", "critical")))
        assertFalse(filter.shouldInclude("ProfileCard", setOf("card")))
    }

    @Test
    fun `excludedTags removes matching composables`() {
        val filter = RecompositionTagFilter(excludedTags = setOf("low-priority"))
        assertTrue(filter.shouldInclude("HomeScreen", setOf("screen", "critical")))
        assertFalse(filter.shouldInclude("SettingsItem", setOf("screen", "low-priority")))
    }

    @Test
    fun `exclusion takes priority over inclusion`() {
        val filter = RecompositionTagFilter(
            includedTags = setOf("screen"),
            excludedTags = setOf("low-priority")
        )
        // "screen" included but "low-priority" excluded → excluded wins
        assertFalse(filter.shouldInclude("SettingsItem", setOf("screen", "low-priority")))
        // "screen" included, no exclusion → passes
        assertTrue(filter.shouldInclude("HomeScreen", setOf("screen", "critical")))
    }

    // ── filter(map) ────────────────────────────────────────────────────────────

    @Test
    fun `filter returns all entries when no tags configured`() {
        val counts = mapOf("HomeScreen" to 5, "ProfileCard" to 3)
        val result = RecompositionTagFilter.PASS_ALL.filter(counts, ::lookup)
        assertEquals(counts, result)
    }

    @Test
    fun `filter keeps only includedTags entries`() {
        val filter = RecompositionTagFilter(includedTags = setOf("critical"))
        val counts = mapOf(
            "HomeScreen"    to 10,
            "ProfileCard"   to 4,
            "SettingsItem"  to 2
        )
        val result = filter.filter(counts, ::lookup)
        assertEquals(mapOf("HomeScreen" to 10), result)
    }

    @Test
    fun `filter removes excludedTags entries`() {
        val filter = RecompositionTagFilter(excludedTags = setOf("animation"))
        val counts = mapOf(
            "HomeScreen"     to 8,
            "LoadingSpinner" to 15
        )
        val result = filter.filter(counts, ::lookup)
        assertEquals(mapOf("HomeScreen" to 8), result)
    }

    @Test
    fun `filter returns empty map when all entries excluded`() {
        val filter = RecompositionTagFilter(excludedTags = setOf("screen", "card", "animation"))
        val counts = mapOf(
            "HomeScreen"     to 5,
            "ProfileCard"    to 3,
            "LoadingSpinner" to 7
        )
        val result = filter.filter(counts, ::lookup)
        assertTrue(result.isEmpty())
    }
}
