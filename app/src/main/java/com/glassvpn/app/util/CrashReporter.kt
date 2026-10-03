package com.glassvpn.app.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Captures uncaught exceptions (app crashes) to a file, since logcat
 * is not readable on some devices. The diagnostic exporter includes it.
 */
object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val DIR = "crashes"
    private const val MAX_FILES = 5

    fun install(context: Context) {
        val appContext = context.applicationContext
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrash(appContext, thread, throwable)
            } catch (e: Exception) {
                Log.e(TAG, "failed to write crash log", e)
            }
            // Let the system handle it (show crash dialog / kill process)
            prev?.uncaughtException(thread, throwable)
        }
    }

    /** Append a breadcrumb line; ring buffer capped at ~200 lines. */
    fun breadcrumb(context: Context, msg: String) {
        try {
            val dir = File(context.applicationContext.filesDir, DIR).apply { mkdirs() }
            val f = File(dir, "breadcrumbs.txt")
            val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            val lines = if (f.exists()) f.readLines().toMutableList() else mutableListOf()
            lines.add("[$ts] $msg")
            while (lines.size > 200) lines.removeAt(0)
            f.writeText(lines.joinToString("\n"))
        } catch (e: Exception) {
            Log.e(TAG, "breadcrumb failed", e)
        }
    }

    fun getBreadcrumbs(context: Context): String {
        return try {
            val f = File(File(context.applicationContext.filesDir, DIR), "breadcrumbs.txt")
            if (f.exists()) f.readText() else "(no breadcrumbs)"
        } catch (e: Exception) {
            "(breadcrumb read failed: ${e.message})"
        }
    }

    fun getLatestCrash(context: Context): String {
        return try {
            val dir = File(context.applicationContext.filesDir, DIR)
            val latest = dir.listFiles { f -> f.name.startsWith("crash-") }
                ?.maxByOrNull { it.lastModified() }
            latest?.readText() ?: "(no crash recorded)"
        } catch (e: Exception) {
            "(crash read failed: ${e.message})"
        }
    }

    fun clearAfterExport(context: Context) {
        // Keep crashes; breadcrumbs rotate on their own.
    }

    private fun writeCrash(context: Context, thread: Thread, t: Throwable) {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val f = File(dir, "crash-$ts.txt")
        val sb = StringBuilder()
        sb.appendLine("=== Crash $ts ===")
        sb.appendLine("Thread: ${thread.name}")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("App: ${UpdateChecker.currentVersionName(context)}")
        sb.appendLine()
        sb.appendLine(Log.getStackTraceString(t))
        // Walk causal chain explicitly (getStackTraceString usually includes it, belt & braces)
        var cause = t.cause
        while (cause != null) {
            sb.appendLine("Caused by:")
            sb.appendLine(Log.getStackTraceString(cause))
            cause = cause.cause
        }
        f.writeText(sb.toString())
        // Prune old crash files
        dir.listFiles { x -> x.name.startsWith("crash-") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_FILES)
            ?.forEach { it.delete() }
    }
}
