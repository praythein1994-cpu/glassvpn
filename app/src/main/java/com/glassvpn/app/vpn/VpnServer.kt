package com.glassvpn.app.vpn

data class VpnServer(
    val hostName: String,
    val ip: String,
    val countryLong: String,
    val countryShort: String,
    val ping: Int,
    val speed: Long,
    val sessions: Int,
    val uptime: Long,
    val openVpnConfigBase64: String,
    var measuredPing: Int = -1
) {
    val displayPing: Int get() = if (measuredPing >= 0) measuredPing else ping
}
