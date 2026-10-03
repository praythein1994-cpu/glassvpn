package com.glassvpn.app.vpn

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object VpnGateClient {
    private const val TAG = "VpnGateClient"
    private const val API_URL = "http://www.vpngate.net/api/iphone/"

    // Preferred countries first; all other VPNGate countries follow (availability changes daily)
    private val PRIORITY = mapOf(
        "SG" to 0, "TH" to 1, "MY" to 2, "PH" to 3,
        "JP" to 4, "KR" to 5, "US" to 6, "MM" to 7
    )

    /** Last fetch failure description, for the diagnostic report. Null if last fetch OK. */
    @Volatile var lastError: String? = null
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchServers(): List<VpnServer> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(API_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                lastError = "HTTP ${response.code}"
                Log.e(TAG, "fetchServers: HTTP ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: run {
                lastError = "empty response body"
                return@withContext emptyList()
            }
            lastError = null
            parseCsv(body)
        } catch (e: Exception) {
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            Log.e(TAG, "fetchServers failed", e)
            emptyList()
        }
    }

    private fun parseCsv(csv: String): List<VpnServer> {
        val servers = mutableListOf<VpnServer>()
        // Skip header lines starting with * or #
        val lines = csv.lines().filter { it.isNotBlank() && !it.startsWith("*") && !it.startsWith("#") }
        for (line in lines) {
            try {
                // VPNGate CSV: 15 columns, last one is base64 config (no commas inside base64)
                val parts = line.split(",")
                if (parts.size < 15) continue
                val countryShort = parts[6].trim()
                if (countryShort.length != 2) continue
                val configB64 = parts[14]
                if (configB64.isBlank()) continue
                // Validate base64 decodes
                Base64.decode(configB64, Base64.DEFAULT)
                servers.add(
                    VpnServer(
                        hostName = parts[0],
                        ip = parts[1],
                        countryLong = parts[5],
                        countryShort = countryShort,
                        ping = parts[3].toIntOrNull() ?: 999,
                        speed = parts[4].toLongOrNull() ?: 0,
                        sessions = parts[7].toIntOrNull() ?: 0,
                        uptime = parts[8].toLongOrNull() ?: 0,
                        openVpnConfigBase64 = configB64
                    )
                )
            } catch (_: Exception) { /* skip bad lines */ }
        }
        // Sort: preferred countries first, then by ping
        return servers.sortedWith(compareBy({ PRIORITY[it.countryShort] ?: 9 }, { it.ping }))
    }

    fun decodeConfig(server: VpnServer): String {
        return String(Base64.decode(server.openVpnConfigBase64, Base64.DEFAULT))
    }
}
