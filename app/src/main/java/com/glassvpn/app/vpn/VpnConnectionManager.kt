package com.glassvpn.app.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.SystemClock
import android.util.Log
import com.glassvpn.app.util.CrashReporter
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.StringReader

enum class VpnConnState { DISCONNECTED, CONNECTING, CONNECTED }

class VpnConnectionManager(private val context: Context) : VpnStatus.StateListener {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var timeoutJob: Job? = null
    private var lastConnectAttempt = 0L

    companion object {
        /** Ignore connect taps within this window — stops accidental double-connects. */
        private const val CONNECT_DEBOUNCE_MS = 2000L
        /** Give up a stuck connection attempt after this long. */
        private const val CONNECT_TIMEOUT_MS = 25000L
    }

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
        // Debounce: ignore rapid re-taps
        val now = SystemClock.elapsedRealtime()
        if (now - lastConnectAttempt < CONNECT_DEBOUNCE_MS) {
            CrashReporter.breadcrumb(context, "connect() debounced (rapid re-tap)")
            return
        }
        lastConnectAttempt = now

        CrashReporter.breadcrumb(context, "connect() called for ${server.ip} (${server.countryShort})")
        try {
            // Stop the watchdog for any previous attempt so they can't overlap
            cancelTimeout()

            _state.value = VpnConnState.CONNECTING
            _statusText.value = "Connecting..."
            currentServer = server

            CrashReporter.breadcrumb(context, "decoding config...")
            val configStr = VpnGateClient.decodeConfig(server)
            CrashReporter.breadcrumb(context, "config decoded, ${configStr.length} chars; parsing...")
            val parser = ConfigParser()
            parser.parseConfig(StringReader(configStr))
            val profile = parser.convertProfile()
            profile.mName = "GlassVPN-${server.countryShort}-${server.ip}"
            CrashReporter.breadcrumb(context, "profile parsed; saving temporary profile...")

            // Save profile so OpenVPNService can use it
            ProfileManager.setTemporaryProfile(context, profile)
            CrashReporter.breadcrumb(context, "profile saved; starting OpenVPN service...")

            val intent = Intent(context, OpenVPNService::class.java)
            intent.putExtra(OpenVPNService.EXTRA_START_REASON, "GlassVPN connect")

            VPNLaunchHelper.startOpenVpn(profile, context, "GlassVPN connect", true)
            CrashReporter.breadcrumb(context, "startOpenVpn returned (service starting async)")

            // Watchdog: if we're still connecting after the timeout, give up cleanly
            scheduleTimeout(server)
        } catch (t: Throwable) {
            CrashReporter.breadcrumb(context, "connect() FAILED: ${t::class.java.simpleName}: ${t.message}")
            Log.e("VpnConnMgr", "connect failed", t)
            cancelTimeout()
            _state.value = VpnConnState.DISCONNECTED
            _statusText.value = "Failed: ${t.message}"
            if (t is Error) throw t // don't swallow fatal errors, but we logged breadcrumbs first
        }
    }

    fun disconnect() {
        CrashReporter.breadcrumb(context, "disconnect() called")
        cancelTimeout()
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

    private fun scheduleTimeout(server: VpnServer) {
        cancelTimeout()
        timeoutJob = scope.launch {
            delay(CONNECT_TIMEOUT_MS)
            if (_state.value == VpnConnState.CONNECTING && currentServer?.ip == server.ip) {
                CrashReporter.breadcrumb(context, "connect timed out for ${server.ip}")
                disconnect()
                _statusText.value = "Connection timed out — try another server"
            }
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    // ---- VpnStatus.StateListener ----
    override fun updateState(
        state: String?,
        logmessage: String?,
        localizedResId: Int,
        level: de.blinkt.openvpn.core.ConnectionStatus?,
        intent: Intent?
    ) {
        val newState = when (state) {
            "CONNECTED" -> VpnConnState.CONNECTED
            "NOPROCESS", "EXITING", "DISCONNECTED" -> VpnConnState.DISCONNECTED
            else -> VpnConnState.CONNECTING
        }
        if (newState != VpnConnState.CONNECTING) cancelTimeout()
        // Don't let a stale service callback override an explicit user disconnect,
        // and don't blank a timeout message with an empty service message.
        if (newState == VpnConnState.DISCONNECTED && _state.value == VpnConnState.DISCONNECTED
            && _statusText.value.isNotBlank()
        ) {
            return
        }
        if (logmessage != null && logmessage.isNotBlank()) _statusText.value = logmessage
        _state.value = newState
    }

    override fun setConnectedVPN(uuid: String?) {}
}
