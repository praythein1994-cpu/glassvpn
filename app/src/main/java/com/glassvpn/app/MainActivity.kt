package com.glassvpn.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glassvpn.app.ui.components.GlassScaffold
import com.glassvpn.app.ui.components.IOSTabBar
import com.glassvpn.app.ui.screens.HomeScreen
import com.glassvpn.app.ui.screens.ServerListScreen
import com.glassvpn.app.ui.screens.SettingsScreen
import com.glassvpn.app.ui.theme.stringsFor
import com.glassvpn.app.util.PingUtil
import com.glassvpn.app.util.PrefsManager
import com.glassvpn.app.vpn.VpnConnState
import com.glassvpn.app.vpn.VpnConnectionManager
import com.glassvpn.app.vpn.VpnGateClient
import com.glassvpn.app.vpn.VpnServer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var vpnManager: VpnConnectionManager
    private var pendingServer: VpnServer? = null

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingServer?.let { vpnManager.connect(it) }
        }
        pendingServer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnManager = VpnConnectionManager(applicationContext)

        setContent {
            val scope = rememberCoroutineScope()
            var tab by remember { mutableIntStateOf(0) }
            var lang by remember { mutableStateOf("en") }
            var servers by remember { mutableStateOf<List<VpnServer>>(emptyList()) }
            var loading by remember { mutableStateOf(false) }
            var selectedServer by remember { mutableStateOf<VpnServer?>(null) }

            val connState by vpnManager.state.collectAsState()
            val statusText by vpnManager.statusText.collectAsState()
            val s = stringsFor(lang)

            // Load language + servers on start
            LaunchedEffect(Unit) {
                lang = PrefsManager.getLanguage(this@MainActivity)
                loadServers(
                    onResult = { list ->
                        servers = list
                        // Restore last server
                        scope.launch {
                            val lastIp = PrefsManager.getLastServerIp(this@MainActivity)
                            selectedServer = list.find { it.ip == lastIp } ?: list.firstOrNull()
                        }
                        // Measure real ping in background for top servers
                        scope.launch { measurePings(list.take(12)) { servers = it } }
                    },
                    setLoading = { loading = it }
                )
            }

            fun refreshServers() {
                scope.launch {
                    loadServers(
                        onResult = { list ->
                            servers = list
                            scope.launch { measurePings(list.take(12)) { servers = it } }
                        },
                        setLoading = { loading = it }
                    )
                }
            }

            fun doConnect(server: VpnServer) {
                selectedServer = server
                scope.launch { PrefsManager.setLastServerIp(this@MainActivity, server.ip) }
                if (vpnManager.needsVpnPermission()) {
                    pendingServer = server
                    vpnManager.permissionIntent()?.let { vpnPermissionLauncher.launch(it) }
                } else {
                    vpnManager.connect(server)
                }
            }

            GlassScaffold {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            when (tab) {
                                0 -> HomeScreen(
                                    s = s,
                                    connState = connState,
                                    statusText = statusText,
                                    server = selectedServer,
                                    onPowerClick = {
                                        if (connState == VpnConnState.CONNECTED ||
                                            connState == VpnConnState.CONNECTING
                                        ) {
                                            vpnManager.disconnect()
                                        } else {
                                            selectedServer?.let { doConnect(it) }
                                                ?: run { tab = 1 }
                                        }
                                    },
                                    onSelectServer = { tab = 1 },
                                    modifier = Modifier.fillMaxSize()
                                )
                                1 -> ServerListScreen(
                                    s = s,
                                    servers = servers,
                                    loading = loading,
                                    selectedIp = selectedServer?.ip,
                                    onSelect = { server ->
                                        selectedServer = server
                                        scope.launch {
                                            PrefsManager.setLastServerIp(
                                                this@MainActivity, server.ip
                                            )
                                        }
                                        // Auto-connect on select if not connected
                                        if (connState == VpnConnState.DISCONNECTED) {
                                            doConnect(server)
                                        }
                                        tab = 0
                                    },
                                    onRefresh = { refreshServers() },
                                    modifier = Modifier.fillMaxSize()
                                )
                                2 -> SettingsScreen(
                                    s = s,
                                    language = lang,
                                    onLanguageChange = { newLang ->
                                        lang = newLang
                                        scope.launch {
                                            PrefsManager.setLanguage(
                                                this@MainActivity, newLang
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    // iOS tab bar at bottom
                    IOSTabBar(
                        tabs = listOf(s.home, s.servers, s.settings),
                        icons = listOf("🏠", "🌐", "⚙️"),
                        selectedIndex = tab,
                        onSelect = { tab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    private suspend fun loadServers(
        onResult: (List<VpnServer>) -> Unit,
        setLoading: (Boolean) -> Unit
    ) {
        setLoading(true)
        try {
            onResult(VpnGateClient.fetchServers())
        } finally {
            setLoading(false)
        }
    }

    private suspend fun measurePings(
        list: List<VpnServer>,
        onUpdate: (List<VpnServer>) -> Unit
    ) {
        // Measure in background; update list as results come in
        val updated = list.toMutableList()
        for ((i, server) in list.withIndex()) {
            val ms = PingUtil.measurePingMs(server.ip)
            if (ms >= 0) {
                updated[i] = server.copy(measuredPing = ms)
                onUpdate(updated.toList())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
