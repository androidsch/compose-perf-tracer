package com.composeperf.tracer

import java.util.UUID

/**
 * Represents a bounded tracking session for recompositions.
 * Sessions can be started, paused, resumed, and stopped.
 */
data class RecompositionSession(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val startedAt: Long = System.currentTimeMillis(),
    val state: SessionState = SessionState.ACTIVE
) {
    enum class SessionState {
        ACTIVE,
        PAUSED,
        STOPPED
    }

    fun isActive(): Boolean = state == SessionState.ACTIVE

    fun pause(): RecompositionSession = copy(state = SessionState.PAUSED)

    fun resume(): RecompositionSession = copy(state = SessionState.ACTIVE)

    fun stop(): RecompositionSession = copy(state = SessionState.STOPPED)

    fun durationMs(): Long = System.currentTimeMillis() - startedAt

    override fun toString(): String =
        "Session(id=$id, label='$label', state=$state, durationMs=${durationMs()}ms)"
}
