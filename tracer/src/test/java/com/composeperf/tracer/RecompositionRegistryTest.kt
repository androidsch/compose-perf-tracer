package com.composeperf.tracer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecompositionRegistryTest {

    @Before
    fun setUp() {
        RecompositionRegistry.clear()
    }

    @After
    fun tearDown() {
        RecompositionRegistry.clear()
    }

    @Test
    fun `register adds counter to registry`() {
        val counter = RecompositionCounter("HomeScreen")
        counter.increment()

        RecompositionRegistry.register(counter)

        assertEquals(1, RecompositionRegistry.getCount("HomeScreen"))
    }

    @Test
    fun `snapshot returns all registered counters`() {
        val c1 = RecompositionCounter("ScreenA").also { it.increment(); it.increment() }
        val c2 = RecompositionCounter("ScreenB").also { it.increment() }

        RecompositionRegistry.register(c1)
        RecompositionRegistry.register(c2)

        val snapshot = RecompositionRegistry.snapshot()

        assertEquals(2, snapshot.size)
        assertEquals(2, snapshot["ScreenA"])
        assertEquals(1, snapshot["ScreenB"])
    }

    @Test
    fun `remove deletes counter from registry`() {
        val counter = RecompositionCounter("DetailScreen")
        RecompositionRegistry.register(counter)
        RecompositionRegistry.remove("DetailScreen")

        assertNull(RecompositionRegistry.getCount("DetailScreen"))
    }

    @Test
    fun `getCount returns null for unregistered name`() {
        assertNull(RecompositionRegistry.getCount("NonExistent"))
    }

    @Test
    fun `registering same name twice replaces previous counter`() {
        val first = RecompositionCounter("Feed").also { it.increment(); it.increment() }
        val second = RecompositionCounter("Feed").also { it.increment() }

        RecompositionRegistry.register(first)
        RecompositionRegistry.register(second)

        assertEquals(1, RecompositionRegistry.getCount("Feed"))
    }

    @Test
    fun `clear removes all counters`() {
        RecompositionRegistry.register(RecompositionCounter("A"))
        RecompositionRegistry.register(RecompositionCounter("B"))
        RecompositionRegistry.clear()

        assertEquals(emptyMap<String, Int>(), RecompositionRegistry.snapshot())
    }
}
