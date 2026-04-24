package com.composeperf.tracer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RecompositionExporterTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var outputDir: File

    @Before
    fun setUp() {
        outputDir = tempFolder.newFolder("exports")
    }

    private fun makeSnapshot(
        entries: Map<String, Int> = emptyMap(),
        capturedAt: Long = 1_000_000L
    ): RecompositionSnapshot = RecompositionSnapshot(
        entries = entries,
        capturedAt = capturedAt
    )

    // --- CsvRecompositionExporter ---

    @Test
    fun `csv exporter creates file in output directory`() {
        val exporter = CsvRecompositionExporter(outputDir)
        val snapshot = makeSnapshot(mapOf("HomeScreen" to 3, "ProfileCard" to 7))

        exporter.export(snapshot)

        val files = outputDir.listFiles() ?: emptyArray()
        assertEquals(1, files.size)
        assertTrue(files[0].name.startsWith("recompositions_"))
        assertTrue(files[0].name.endsWith(".csv"))
    }

    @Test
    fun `csv exporter writes header and entries`() {
        val exporter = CsvRecompositionExporter(outputDir)
        val snapshot = makeSnapshot(mapOf("ButtonA" to 2), capturedAt = 1_000_000L)

        exporter.export(snapshot)

        val csvFile = outputDir.listFiles()!!.first()
        val lines = csvFile.readLines()

        assertEquals("composable,count,captured_at", lines[0])
        assertTrue(lines.any { it.startsWith("ButtonA,2,") })
    }

    @Test
    fun `csv exporter escapes composable names with commas`() {
        val exporter = CsvRecompositionExporter(outputDir)
        val snapshot = makeSnapshot(mapOf("Screen, Main" to 5))

        exporter.export(snapshot)

        val csvFile = outputDir.listFiles()!!.first()
        val content = csvFile.readText()

        assertTrue(content.contains('"'))
        assertTrue(content.contains("Screen, Main"))
    }

    @Test
    fun `csv exporter writes empty body for empty snapshot`() {
        val exporter = CsvRecompositionExporter(outputDir)
        val snapshot = makeSnapshot()

        exporter.export(snapshot)

        val csvFile = outputDir.listFiles()!!.first()
        val lines = csvFile.readLines().filter { it.isNotBlank() }

        assertEquals(1, lines.size) // only header
        assertEquals("composable,count,captured_at", lines[0])
    }

    // --- LogRecompositionExporter ---

    @Test
    fun `log exporter does not throw for non-empty snapshot`() {
        val exporter = LogRecompositionExporter(tag = "TestTag")
        val snapshot = makeSnapshot(mapOf("ToolbarComposable" to 10))

        // Should complete without exception
        exporter.export(snapshot)
    }

    @Test
    fun `log exporter does not throw for empty snapshot`() {
        val exporter = LogRecompositionExporter()
        val snapshot = makeSnapshot()

        exporter.export(snapshot)
    }
}
