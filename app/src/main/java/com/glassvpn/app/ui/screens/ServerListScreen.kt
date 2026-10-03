package com.glassvpn.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glassvpn.app.ui.components.GlassCard
import com.glassvpn.app.ui.components.IOSListRow
import com.glassvpn.app.ui.components.PingBadge
import com.glassvpn.app.ui.theme.AppStrings
import com.glassvpn.app.ui.theme.GlassColors
import com.glassvpn.app.ui.theme.countryFlag
import com.glassvpn.app.vpn.VpnServer

@Composable
fun ServerListScreen(
    s: AppStrings,
    servers: List<VpnServer>,
    loading: Boolean,
    selectedIp: String?,
    onSelect: (VpnServer) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // iOS large title header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                s.servers,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = GlassColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (!loading) {
                Text(
                    "↻ ${s.refreshServers}",
                    fontSize = 15.sp,
                    color = GlassColors.iosBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRefresh
                        )
                        .padding(8.dp)
                )
            }
        }

        // Make refresh tappable via a simple clickable on the header row
        // (refresh handled by parent through onRefresh on pull — here via button below)

        if (loading && servers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GlassColors.iosBlue)
                    Spacer(Modifier.height(12.dp))
                    Text(s.loadingServers, color = GlassColors.textSecondary, fontSize = 14.sp)
                }
            }
            return
        }

        if (servers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛰️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.noServers,
                        color = GlassColors.textSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    GlassCard(onClick = onRefresh, cornerRadius = 14.dp) {
                        Text(
                            "↻ ${s.refreshServers}",
                            color = GlassColors.iosBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            return
        }

        // Group by country — iOS grouped list style
        val grouped = servers.groupBy { it.countryShort }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            grouped.forEach { (code, list) ->
                item(key = "header_$code") {
                    Text(
                        "${countryFlag(code)} ${list.first().countryLong.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassColors.textTertiary,
                        modifier = Modifier.padding(start = 14.dp, bottom = 2.dp)
                    )
                }
                item(key = "group_$code") {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassColors.glassBrush())
                    ) {
                        // Fastest badge on the lowest-ping server
                        val fastest = list.minByOrNull { it.displayPing }
                        list.sortedBy { it.displayPing }.take(8).forEachIndexed { idx, server ->
                            IOSListRow(
                                title = server.ip,
                                subtitle = "${s.sessions}: ${server.sessions}",
                                leading = {
                                    if (server == fastest && server.displayPing >= 0) {
                                        Text(
                                            "⚡",
                                            fontSize = 18.sp
                                        )
                                    }
                                },
                                trailing = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (server.ip == selectedIp) {
                                            Text("✓", color = GlassColors.iosBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        PingBadge(server.displayPing)
                                    }
                                },
                                onClick = { onSelect(server) }
                            )
                            if (idx < minOf(8, list.size) - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 60.dp)
                                        .height(1.dp)
                                        .background(GlassColors.glassBorder.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}
