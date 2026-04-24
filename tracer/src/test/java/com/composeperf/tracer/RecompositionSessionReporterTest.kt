package com.composeperf.tracer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionSessionReporterTest {

    private lateinit var sessionManager: RecompositionSessionManager
    private lateinit var registry: RecompositionRegistry
    private lateinit var reporter: RecompositionSessionReporter

    @Before
    fun setUp() {
        sessionManager = RecompositionSessionManager()
        registry = RecompositionRegistry()
        reporter = RecompositionSessionReporter(sessionManager, registry)
    }

    @Test
    fun `reportSession returns not found message for unknown session`() {
        val report = reporter.reportSession("unknown-id")
        assertTrue(report.contains("not found"))
    }

    @Test
    fun `reportSession includes session label and state`() {
        val session = sessionManager.startSession("DashboardScreen")
        val report = reporter.reportSession(session.id)
        assertTrue(report.contains("DashboardScreen"))
        assertTrue(report.contains("Session Report"))
    }

    @Test
    fun `reportSession shows no recompositions when registry is empty`() {
        val session = sessionManager.startSession("EmptyScreen")
        val report = reporter.reportSession(session.id)
        assertTrue(report.contains("No recompositions recorded"))
    }

    @Test
    fun `reportSession lists composables with counts`() {
        val session = sessionManager.startSession("ListScreen")
        registry.record("ItemRow")
        registry.record("ItemRow")
        registry.record("Header")
        val report = reporter.reportSession(session.id)
        assertTrue(report.contains("ItemRow"))
        assertTrue(report.contains("Header"))
        assertTrue(report.contains("2 recomposition"))
    }

    @Test
    fun `reportAllActiveSessions returns reports for all active sessions`() {
        sessionManager.startSession("ScreenA")
        sessionManager.startSession("ScreenB")
        val reports = reporter.reportAllActiveSessions()
        assertEquals(2, reports.size)
    }

    @Test
    fun `reportAllActiveSessions excludes stopped sessions`() {
        sessionManager.startSession("Active")
        val stopped = sessionManager.startSession("Stopped")
        sessionManager.stopSession(stopped.id)
        val reports = reporter.reportAllActiveSessions()
        assertEquals(1, reports.size)
    }
}
