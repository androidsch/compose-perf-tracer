package com.composeperf.tracer

/**
 * Produces a human-readable or structured report for a given [RecompositionSession],
 * combining session metadata with snapshot data from the registry.
 */
class RecompositionSessionReporter(
    private val sessionManager: RecompositionSessionManager,
    private val registry: RecompositionRegistry
) {

    fun reportSession(sessionId: String): String {
        val session = sessionManager.getSession(sessionId)
            ?: return "Session '$sessionId' not found."

        val snapshot = registry.snapshot()
        val entries = snapshot.entries

        if (entries.isEmpty()) {
            return buildString {
                appendLine("=== Session Report ===")
                appendLine(session.toString())
                appendLine("No recompositions recorded.")
            }
        }

        return buildString {
            appendLine("=== Session Report ===")
            appendLine(session.toString())
            appendLine("Recompositions (${entries.size} components):")
            entries
                .sortedByDescending { it.count }
                .forEach { entry ->
                    appendLine("  ${entry.composableName}: ${entry.count} recomposition(s)")
                }
        }
    }

    fun reportAllActiveSessions(): List<String> =
        sessionManager.getActiveSessions().map { reportSession(it.id) }
}
