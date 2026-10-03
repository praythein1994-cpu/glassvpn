package com.glassvpn.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glassvpn.app.ui.components.GlassCard
import com.glassvpn.app.ui.components.PingBadge
import com.glassvpn.app.ui.components.PowerButton
import com.glassvpn.app.ui.theme.AppStrings
import com.glassvpn.app.ui.theme.GlassColors
import com.glassvpn.app.ui.theme.countryFlag
import com.glassvpn.app.vpn.VpnConnState
import com.glassvpn.app.vpn.VpnServer

@Composable
fun HomeScreen(
    s: AppStrings,
    connState: VpnConnState,
    statusText: String,
    server: VpnServer?,
    onPowerClick: () -> Unit,
    onSelectServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))

        // iOS large title
        Text(
            s.appName,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = GlassColors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            when (connState) {
                VpnConnState.CONNECTED -> s.connected
                VpnConnState.CONNECTING -> s.connecting
                VpnConnState.DISCONNECTED -> s.disconnected
            },
            fontSize = 15.sp,
            color = when (connState) {
                VpnConnState.CONNECTED -> GlassColors.iosGreen
                VpnConnState.CONNECTING -> GlassColors.iosOrange
                VpnConnState.DISCONNECTED -> GlassColors.textSecondary
            },
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(28.dp))

        PowerButton(
            connected = connState == VpnConnState.CONNECTED,
            connecting = connState == VpnConnState.CONNECTING,
            onClick = onPowerClick
        )

        Spacer(Modifier.height(12.dp))
        Text(
            when (connState) {
                VpnConnState.CONNECTED -> s.tapToDisconnect
                VpnConnState.CONNECTING -> statusText.ifBlank { s.connecting }
                VpnConnState.DISCONNECTED -> s.tapToConnect
            },
            fontSize = 13.sp,
            color = GlassColors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(26.dp))

        // Current server card (iOS style)
        GlassCard(onClick = onSelectServer, modifier = Modifier.fillMaxWidth()) {
            Text(
                s.currentServer,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassColors.textTertiary
            )
            Spacer(Modifier.height(8.dp))
            if (server != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(countryFlag(server.countryShort), fontSize = 34.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            server.countryLong,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassColors.textPrimary
                        )
                        Text(
                            server.ip,
                            fontSize = 13.sp,
                            color = GlassColors.textSecondary
                        )
                    }
                    PingBadge(server.displayPing)
                }
            } else {
                Text(
                    s.noServerSelected,
                    fontSize = 16.sp,
                    color = GlassColors.textSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    s.selectServer + " ›",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassColors.iosBlue
                )
            }
        }
    }
}
