package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionSessionManagerTest {

    private lateinit var manager: RecompositionSessionManager

    @Before
    fun setUp() {
        manager = RecompositionSessionManager()
    }

    @Test
    fun `startSession creates active session with given label`() {
        val session = manager.startSession("HomeScreen")
        assertEquals("HomeScreen", session.label)
        assertTrue(session.isActive())
    }

    @Test
    fun `pauseSession transitions session to PAUSED`() {
        val session = manager.startSession("ProfileScreen")
        val paused = manager.pauseSession(session.id)
        assertNotNull(paused)
        assertEquals(RecompositionSession.SessionState.PAUSED, paused!!.state)
    }

    @Test
    fun `resumeSession transitions session back to ACTIVE`() {
        val session = manager.startSession("FeedScreen")
        manager.pauseSession(session.id)
        val resumed = manager.resumeSession(session.id)
        assertNotNull(resumed)
        assertTrue(resumed!!.isActive())
    }

    @Test
    fun `stopSession transitions session to STOPPED`() {
        val session = manager.startSession("SettingsScreen")
        val stopped = manager.stopSession(session.id)
        assertNotNull(stopped)
        assertEquals(RecompositionSession.SessionState.STOPPED, stopped!!.state)
    }

    @Test
    fun `getActiveSessions returns only active sessions`() {
        val s1 = manager.startSession("A")
        val s2 = manager.startSession("B")
        manager.stopSession(s2.id)
        val active = manager.getActiveSessions()
        assertEquals(1, active.size)
        assertEquals(s1.id, active.first().id)
    }

    @Test
    fun `clearStoppedSessions removes only stopped sessions`() {
        manager.startSession("Active")
        val stopped = manager.startSession("Stopped")
        manager.stopSession(stopped.id)
        manager.clearStoppedSessions()
        val all = manager.getAllSessions()
        assertEquals(1, all.size)
        assertTrue(all.first().isActive())
    }

    @Test
    fun `pauseSession returns null for unknown session id`() {
        val result = manager.pauseSession("non-existent-id")
        assertNull(result)
    }

    @Test
    fun `reset clears all sessions`() {
        manager.startSession("X")
        manager.startSession("Y")
        manager.reset()
        assertTrue(manager.getAllSessions().isEmpty())
    }
}
