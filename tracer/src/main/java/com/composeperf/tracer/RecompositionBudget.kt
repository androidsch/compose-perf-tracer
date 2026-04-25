package com.composeperf.tracer

/**
 * Defines a per-composable recomposition budget — the maximum number of recompositions
 * allowed within a given time window before a violation is reported.
 */
data class RecompositionBudget(
    val composableName: String,
    val maxCount: Int,
    val windowMillis: Long = 1_000L
)

/**
 * Represents the result of a budget evaluation.
 */
data class BudgetViolation(
    val composableName: String,
    val budget: RecompositionBudget,
    val actualCount: Int,
    val windowMillis: Long
) {
    val excess: Int get() = actualCount - budget.maxCount

    override fun toString(): String =
        "[BudgetViolation] '$composableName' recomposed $actualCount times " +
            "(budget: ${budget.maxCount}, excess: $excess) " +
            "in ${windowMillis}ms window"
}

/**
 * Evaluates recomposition counts against registered budgets and surfaces violations.
 */
class RecompositionBudgetEnforcer(
    private val budgets: List<RecompositionBudget>,
    private val onViolation: (BudgetViolation) -> Unit = { violation ->
        android.util.Log.w("RecompositionBudget", violation.toString())
    }
) {
    private val windowCounts: MutableMap<String, MutableList<Long>> = mutableMapOf()

    /**
     * Records a recomposition event at the given timestamp (ms) and checks the budget.
     */
    fun record(composableName: String, timestampMillis: Long = System.currentTimeMillis()) {
        val budget = budgets.firstOrNull { it.composableName == composableName } ?: return

        val timestamps = windowCounts.getOrPut(composableName) { mutableListOf() }
        val windowStart = timestampMillis - budget.windowMillis
        timestamps.removeAll { it < windowStart }
        timestamps.add(timestampMillis)

        if (timestamps.size > budget.maxCount) {
            onViolation(
                BudgetViolation(
                    composableName = composableName,
                    budget = budget,
                    actualCount = timestamps.size,
                    windowMillis = budget.windowMillis
                )
            )
        }
    }

    /** Clears all recorded window data. */
    fun reset() {
        windowCounts.clear()
    }

    /** Returns current window counts for inspection/testing. */
    fun currentCounts(): Map<String, Int> =
        windowCounts.mapValues { it.value.size }
}
