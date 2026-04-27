package com.composeperf.tracer

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionBaselineManagerTest {

    private lateinit var registry: RecompositionRegistry
    private lateinit var manager: RecompositionBaselineManager

    @Before
    fun setUp() {
        registry = mockk()
        manager = RecompositionBaselineManager(registry)
    }

    private fun snapshotOf(vararg pairs: Pair<String, Int>): List<RecompositionSnapshot> =
        pairs.map { (key, count) -> RecompositionSnapshot(key, count) }

    @Test
    fun `capture stores baseline with correct counts`() {
        every { registry.snapshot() } returns snapshotOf("ButtonA" to 3, "TextB" to 5)

        val baseline = manager.capture("initial")

        assertEquals("initial", baseline.label)
        assertEquals(3, baseline.countFor("ButtonA"))
        assertEquals(5, baseline.countFor("TextB"))
        assertEquals(8, baseline.total)
    }

    @Test
    fun `get returns null for unknown label`() {
        assertNull(manager.get("missing"))
    }

    @Test
    fun `get returns stored baseline`() {
        every { registry.snapshot() } returns snapshotOf("CardX" to 2)
        manager.capture("snap1")

        val result = manager.get("snap1")
        assertNotNull(result)
        assertEquals("snap1", result!!.label)
    }

    @Test
    fun `compare returns null for unknown baseline`() {
        assertNull(manager.compare("ghost"))
    }

    @Test
    fun `compare detects increased counts`() {
        every { registry.snapshot() } returns snapshotOf("ButtonA" to 2)
        manager.capture("base")

        every { registry.snapshot() } returns snapshotOf("ButtonA" to 7)
        val diff = manager.compare("base")

        assertNotNull(diff)
        assertTrue(diff!!.hasChanges)
        assertEquals(5, diff.increased["ButtonA"])
        assertTrue(diff.added.isEmpty())
        assertTrue(diff.removed.isEmpty())
    }

    @Test
    fun `compare detects added and removed composables`() {
        every { registry.snapshot() } returns snapshotOf("OldItem" to 1)
        manager.capture("base")

        every { registry.snapshot() } returns snapshotOf("NewItem" to 4)
        val diff = manager.compare("base")

        assertNotNull(diff)
        assertTrue(diff!!.added.containsKey("NewItem"))
        assertTrue(diff.removed.contains("OldItem"))
    }

    @Test
    fun `remove deletes the baseline`() {
        every { registry.snapshot() } returns snapshotOf("X" to 1)
        manager.capture("temp")
        manager.remove("temp")

        assertNull(manager.get("temp"))
    }

    @Test
    fun `clearAll removes all baselines`() {
        every { registry.snapshot() } returns snapshotOf("A" to 1)
        manager.capture("one")
        manager.capture("two")
        manager.clearAll()

        assertTrue(manager.labels().isEmpty())
    }
}
