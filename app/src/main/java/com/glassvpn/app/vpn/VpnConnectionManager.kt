package com.glassvpn.app.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.StringReader

enum class VpnConnState { DISCONNECTED, CONNECTING, CONNECTED }

class VpnConnectionManager(private val context: Context) : VpnStatus.StateListener {

    private val _state = MutableStateFlow(VpnConnState.DISCONNECTED)
    val state: StateFlow<VpnConnState> = _state

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText

    var currentServer: VpnServer? = null
        private set

    init {
        VpnStatus.addStateListener(this)
    }

    fun needsVpnPermission(): Boolean {
        return VpnService.prepare(context) != null
    }

    fun permissionIntent(): Intent? = VpnService.prepare(context)

    fun connect(server: VpnServer) {
        try {
            _state.value = VpnConnState.CONNECTING
            _statusText.value = "Connecting..."
            currentServer = server

            val configStr = VpnGateClient.decodeConfig(server)
            val parser = ConfigParser()
            parser.parseConfig(StringReader(configStr))
            val profile = parser.convertProfile()
            profile.mName = "GlassVPN-${server.countryShort}-${server.ip}"

            // Save profile so OpenVPNService can use it
            ProfileManager.setTemporaryProfile(context, profile)

            val intent = Intent(context, OpenVPNService::class.java)
            intent.putExtra(OpenVPNService.EXTRA_START_REASON, "GlassVPN connect")

            VPNLaunchHelper.startOpenVpn(profile, context, "GlassVPN connect", true)
        } catch (e: Exception) {
            Log.e("VpnConnMgr", "connect failed", e)
            _state.value = VpnConnState.DISCONNECTED
            _statusText.value = "Failed: ${e.message}"
        }
    }

    fun disconnect() {
        try {
            ProfileManager.setTemporaryProfile(context, null)
            val intent = Intent(context, OpenVPNService::class.java)
            intent.action = OpenVPNService.DISCONNECT_VPN
            context.startService(intent)
        } catch (e: Exception) {
            Log.e("VpnConnMgr", "disconnect failed", e)
        } finally {
            _state.value = VpnConnState.DISCONNECTED
            _statusText.value = ""
            currentServer = null
        }
    }

    // ---- VpnStatus.StateListener ----
    override fun updateState(
        state: String?,
        logmessage: String?,
        localizedResId: Int,
        level: de.blinkt.openvpn.core.ConnectionStatus?,
        intent: Intent?
    ) {
        _statusText.value = logmessage ?: ""
        _state.value = when (state) {
            "CONNECTED" -> VpnConnState.CONNECTED
            "NOPROCESS", "EXITING", "DISCONNECTED" -> VpnConnState.DISCONNECTED
            else -> VpnConnState.CONNECTING
        }
    }

    override fun setConnectedVPN(uuid: String?) {}
}
