package com.composeperf.tracer

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages lifecycle of [RecompositionSession] instances.
 * Allows creating, pausing, resuming, and stopping sessions.
 */
class RecompositionSessionManager {

    private val sessions = ConcurrentHashMap<String, RecompositionSession>()

    fun startSession(label: String): RecompositionSession {
        val session = RecompositionSession(label = label)
        sessions[session.id] = session
        return session
    }

    fun pauseSession(sessionId: String): RecompositionSession? {
        val session = sessions[sessionId]?.pause() ?: return null
        sessions[sessionId] = session
        return session
    }

    fun resumeSession(sessionId: String): RecompositionSession? {
        val session = sessions[sessionId]?.resume() ?: return null
        sessions[sessionId] = session
        return session
    }

    fun stopSession(sessionId: String): RecompositionSession? {
        val session = sessions[sessionId]?.stop() ?: return null
        sessions[sessionId] = session
        return session
    }

    fun getSession(sessionId: String): RecompositionSession? = sessions[sessionId]

    fun getActiveSessions(): List<RecompositionSession> =
        sessions.values.filter { it.isActive() }

    fun getAllSessions(): List<RecompositionSession> = sessions.values.toList()

    fun clearStoppedSessions() {
        sessions.entries.removeIf { it.value.state == RecompositionSession.SessionState.STOPPED }
    }

    fun reset() {
        sessions.clear()
    }
}
