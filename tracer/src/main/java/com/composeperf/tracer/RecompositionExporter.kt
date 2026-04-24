package com.composeperf.tracer

import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Strategy interface for exporting recomposition snapshots to various destinations.
 */
fun interface RecompositionExporter {
    fun export(snapshot: RecompositionSnapshot)
}

/**
 * Exports recomposition data as a CSV file to the provided directory.
 *
 * Each row contains: composable name, recomposition count, timestamp.
 */
class CsvRecompositionExporter(
    private val outputDir: File,
    private val tag: String = "RecompositionExporter"
) : RecompositionExporter {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    override fun export(snapshot: RecompositionSnapshot) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            Log.e(tag, "Failed to create output directory: ${outputDir.absolutePath}")
            return
        }

        val timestamp = dateFormat.format(Date(snapshot.capturedAt))
        val file = File(outputDir, "recompositions_$timestamp.csv")

        try {
            file.bufferedWriter().use { writer ->
                writer.write("composable,count,captured_at\n")
                snapshot.entries.forEach { (name, count) ->
                    writer.write("${escapeCsv(name)},$count,${snapshot.capturedAt}\n")
                }
            }
            Log.d(tag, "Exported ${snapshot.entries.size} entries to ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e(tag, "Failed to write CSV export: ${e.message}")
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

/**
 * Exports recomposition data as a simple log dump using Android's Log API.
 */
class LogRecompositionExporter(
    private val tag: String = "RecompositionExport"
) : RecompositionExporter {

    override fun export(snapshot: RecompositionSnapshot) {
        Log.d(tag, "--- Recomposition Snapshot (capturedAt=${snapshot.capturedAt}) ---")
        if (snapshot.entries.isEmpty()) {
            Log.d(tag, "  No recompositions recorded.")
        } else {
            snapshot.entries
                .entries
                .sortedByDescending { it.value }
                .forEach { (name, count) ->
                    Log.d(tag, "  $name -> $count recompositions")
                }
        }
        Log.d(tag, "--- End Snapshot ---")
    }
}
