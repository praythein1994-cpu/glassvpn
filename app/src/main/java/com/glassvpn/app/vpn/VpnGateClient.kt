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

    // Countries the user asked for (Myanmar rarely has free public servers)
    val TARGET_COUNTRIES = setOf("SG", "TH", "MY", "JP", "US", "PH", "MM")

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchServers(): List<VpnServer> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(API_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            parseCsv(body)
        } catch (e: Exception) {
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
                val countryShort = parts[6]
                if (countryShort !in TARGET_COUNTRIES) continue
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
        // Sort: by country priority (SG first for Myanmar users), then ping
        val priority = mapOf("SG" to 0, "TH" to 1, "MY" to 2, "PH" to 3, "JP" to 4, "US" to 5, "MM" to 6)
        return servers.sortedWith(compareBy({ priority[it.countryShort] ?: 9 }, { it.ping }))
    }

    fun decodeConfig(server: VpnServer): String {
        return String(Base64.decode(server.openVpnConfigBase64, Base64.DEFAULT))
    }
}
