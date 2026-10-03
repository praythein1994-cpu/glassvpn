package com.glassvpn.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.Log
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostic tool: gathers app/device/network/VPN state into a human-readable
 * report and saves it as a .txt file in the phone's Download folder so the
 * user can send it for troubleshooting.
 */
object Diagnostics {
    private const val TAG = "Diagnostics"

    data class ReportInput(
        val serverCount: Int,
        val selectedServerIp: String?,
        val vpnState: String,
        val vpnStatusText: String,
        val lastServerFetchError: String?
    )

    suspend fun generateReport(context: Context, input: ReportInput): String =
        withContext(Dispatchers.Default) {
            val sb = StringBuilder()
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            sb.appendLine("=== GlassVPN Diagnostic Report ===")
            sb.appendLine("Generated: $now")
            sb.appendLine()
            sb.appendLine("--- App ---")
            sb.appendLine("Version: ${UpdateChecker.currentVersionName(context)}")
            sb.appendLine("Package: ${context.packageName}")
            sb.appendLine()
            sb.appendLine("--- Device ---")
            sb.appendLine("Model: ${Build.MANUFACTURER} ${Build.MODEL}")
            sb.appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            sb.appendLine("ABI: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            sb.appendLine()
            sb.appendLine("--- Network ---")
            sb.appendLine("Active network: ${activeNetworkType(context)}")
            sb.appendLine()
            sb.appendLine("--- VPN ---")
            sb.appendLine("State: ${input.vpnState}")
            if (input.vpnStatusText.isNotBlank()) sb.appendLine("Status: ${input.vpnStatusText}")
            sb.appendLine("Selected server: ${input.selectedServerIp ?: "none"}")
            sb.appendLine()
            sb.appendLine("--- Servers ---")
            sb.appendLine("Loaded servers: ${input.serverCount}")
            input.lastServerFetchError?.let {
                sb.appendLine("Last fetch error: $it")
            }
            sb.appendLine()
            sb.appendLine("--- VPN log (OpenVPN service messages) ---")
            sb.appendLine(collectVpnLog(context).take(20000))
            sb.appendLine()
            sb.appendLine("--- Crash log (latest uncaught exception) ---")
            sb.appendLine(CrashReporter.getLatestCrash(context).take(20000))
            sb.appendLine()
            sb.appendLine("--- Breadcrumbs (what the app did before the crash) ---")
            sb.appendLine(CrashReporter.getBreadcrumbs(context).take(10000))
            sb.appendLine()
            sb.appendLine("--- Logcat (GlassVPN, last 200 lines) ---")
            sb.appendLine(collectLogcat())
            sb.appendLine()
            sb.appendLine("=== End of report ===")
            sb.toString()
        }

    /** Saves the report as a .txt in Downloads. Returns the file or null. */
    suspend fun exportToDownloads(context: Context, input: ReportInput): File? =
        withContext(Dispatchers.IO) {
            try {
                val report = generateReport(context, input)
                val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "glassvpn-diagnostic-$ts.txt")
                file.writeText(report)
                file
            } catch (e: Exception) {
                Log.e(TAG, "exportToDownloads failed", e)
                null
            }
        }

    private fun activeNetworkType(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return "none"
            val caps = cm.getNetworkCapabilities(net) ?: return "unknown"
            buildList {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) add("WiFi")
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) add("Cellular")
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) add("VPN")
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) add("Ethernet")
            }.joinToString(", ").ifBlank { "unknown" }
        } catch (_: Exception) { "unknown" }
    }

    private fun collectVpnLog(context: Context): String {
        return try {
            val items = VpnStatus.getlogbuffer()
            if (items.isEmpty()) return "(VPN log is empty)"
            // Last 120 entries, oldest first
            items.takeLast(120).joinToString("\n") { it.getString(context) }
        } catch (e: Exception) {
            "(VPN log unavailable: ${e.message})"
        }
    }

    private fun collectLogcat(): String {
        return try {
            val proc = Runtime.getRuntime().exec(
                arrayOf("logcat", "-d", "-t", "200", "GlassVPN:D", "VpnGateClient:D", "VpnConnectionManager:D", "*:S")
            )
            val out = proc.inputStream.bufferedReader().readText()
            proc.waitFor()
            out.ifBlank { "(no log lines captured)" }.take(20000)
        } catch (e: Exception) {
            "(logcat unavailable: ${e.message})"
        }
    }
}
