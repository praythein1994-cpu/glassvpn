package com.glassvpn.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PingUtil {
    /**
     * Measure TCP connect latency to the VPN server's OpenVPN port.
     * VPNGate servers listen on TCP 443 or UDP 1194; we probe TCP 443.
     * Returns ms, or -1 on failure.
     */
    suspend fun measurePingMs(ip: String, port: Int = 443, timeoutMs: Int = 3000): Int =
        withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), timeoutMs)
                }
                (System.currentTimeMillis() - start).toInt()
            } catch (_: Exception) {
                -1
            }
        }
}
