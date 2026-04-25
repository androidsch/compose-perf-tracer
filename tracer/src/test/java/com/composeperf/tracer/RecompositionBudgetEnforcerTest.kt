package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionBudgetEnforcerTest {

    private val violations = mutableListOf<BudgetViolation>()

    private val budgets = listOf(
        RecompositionBudget(composableName = "HomeScreen", maxCount = 3, windowMillis = 1_000L),
        RecompositionBudget(composableName = "ProfileCard", maxCount = 1, windowMillis = 500L)
    )

    private lateinit var enforcer: RecompositionBudgetEnforcer

    @Before
    fun setUp() {
        violations.clear()
        enforcer = RecompositionBudgetEnforcer(budgets) { violations.add(it) }
    }

    @Test
    fun `no violation when recompositions are within budget`() {
        val base = 0L
        repeat(3) { enforcer.record("HomeScreen", base + it * 100L) }
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `violation triggered when recompositions exceed budget`() {
        val base = 0L
        repeat(4) { enforcer.record("HomeScreen", base + it * 100L) }
        assertEquals(1, violations.size)
        val v = violations.first()
        assertEquals("HomeScreen", v.composableName)
        assertEquals(4, v.actualCount)
        assertEquals(1, v.excess)
    }

    @Test
    fun `old recompositions outside window are evicted`() {
        enforcer.record("HomeScreen", 0L)
        enforcer.record("HomeScreen", 100L)
        enforcer.record("HomeScreen", 200L)
        // These three are within budget; now advance past the window
        enforcer.record("HomeScreen", 1_500L) // 0L, 100L, 200L are outside 500ms window from 1500
        assertTrue("Expected no violation but got: $violations", violations.isEmpty())
    }

    @Test
    fun `violation excess is calculated correctly`() {
        val base = 0L
        repeat(6) { enforcer.record("HomeScreen", base + it * 50L) }
        assertTrue(violations.isNotEmpty())
        val last = violations.last()
        assertEquals(last.actualCount - last.budget.maxCount, last.excess)
    }

    @Test
    fun `unregistered composable is ignored`() {
        enforcer.record("UnknownComposable", 0L)
        assertTrue(violations.isEmpty())
        assertEquals(0, enforcer.currentCounts().size)
    }

    @Test
    fun `reset clears all window data`() {
        repeat(3) { enforcer.record("HomeScreen", it * 100L) }
        enforcer.reset()
        assertEquals(0, enforcer.currentCounts().size)
    }

    @Test
    fun `multiple composables tracked independently`() {
        enforcer.record("ProfileCard", 0L)
        enforcer.record("ProfileCard", 100L) // exceeds budget of 1
        assertEquals(1, violations.size)
        assertEquals("ProfileCard", violations.first().composableName)

        // HomeScreen should still be clean
        enforcer.record("HomeScreen", 200L)
        assertEquals(1, violations.size)
    }

    @Test
    fun `toString of BudgetViolation contains key info`() {
        val budget = RecompositionBudget("TestComposable", 2, 1000L)
        val violation = BudgetViolation("TestComposable", budget, 5, 1000L)
        val str = violation.toString()
        assertTrue(str.contains("TestComposable"))
        assertTrue(str.contains("5"))
        assertTrue(str.contains("2"))
        assertTrue(str.contains("3")) // excess
    }
}
