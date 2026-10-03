package com.glassvpn.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glassvpn.app.ui.components.IOSListRow
import com.glassvpn.app.ui.components.IOSSegmentedControl
import com.glassvpn.app.ui.theme.AppStrings
import com.glassvpn.app.ui.theme.GlassColors
import com.glassvpn.app.util.UpdateChecker

/** UI state for the in-app updater. */
sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data object UpToDate : UpdateUiState()
    data class Available(val info: UpdateChecker.UpdateInfo) : UpdateUiState()
    data class Downloading(val progress: Int) : UpdateUiState()
    data object Failed : UpdateUiState()
}

@Composable
fun SettingsScreen(
    s: AppStrings,
    language: String,
    onLanguageChange: (String) -> Unit,
    appVersion: String,
    updateState: UpdateUiState,
    onCheckUpdate: () -> Unit,
    onDownloadUpdate: (UpdateChecker.UpdateInfo) -> Unit,
    diagMessage: String?,
    onExportDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Text(
            s.settings,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = GlassColors.textPrimary,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
        )

        Spacer(Modifier.height(8.dp))

        // ---- Updates section ----
        Text(
            s.checkForUpdates.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassColors.textTertiary,
            modifier = Modifier.padding(start = 36.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassColors.glassBrush())
                .padding(vertical = 4.dp)
        ) {
            IOSListRow(
                title = "GlassVPN",
                subtitle = "${s.version} $appVersion"
            )
            Divider()
            when (updateState) {
                is UpdateUiState.Idle -> IOSListRow(
                    title = "🔄",
                    subtitle = null,
                    trailing = {
                        Text(
                            s.checkForUpdates,
                            color = GlassColors.iosBlue,
                            fontSize = 15.sp
                        )
                    },
                    onClick = onCheckUpdate
                )
                is UpdateUiState.Checking -> IOSListRow(
                    title = s.checkingForUpdates,
                    subtitle = null
                )
                is UpdateUiState.UpToDate -> IOSListRow(
                    title = "✅",
                    subtitle = null,
                    trailing = {
                        Text(s.upToDate, color = GlassColors.iosGreen, fontSize = 15.sp)
                    },
                    onClick = onCheckUpdate
                )
                is UpdateUiState.Available -> {
                    IOSListRow(
                        title = "⬆️ ${s.updateAvailable}",
                        subtitle = "v${updateState.info.latestVersion}",
                        trailing = {
                            Text(
                                s.downloadAndInstall,
                                color = GlassColors.iosBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = { onDownloadUpdate(updateState.info) }
                    )
                    if (updateState.info.releaseNotes.isNotBlank()) {
                        Text(
                            updateState.info.releaseNotes.take(300),
                            fontSize = 13.sp,
                            color = GlassColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }
                is UpdateUiState.Downloading -> Column(
                    Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        "${s.downloading} ${updateState.progress}%",
                        color = GlassColors.textPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { updateState.progress / 100f },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                        color = GlassColors.iosBlue,
                        trackColor = GlassColors.textTertiary.copy(alpha = 0.3f)
                    )
                }
                is UpdateUiState.Failed -> IOSListRow(
                    title = "⚠️ ${s.downloadFailed}",
                    subtitle = null,
                    trailing = {
                        Text(s.retry, color = GlassColors.iosBlue, fontSize = 15.sp)
                    },
                    onClick = onCheckUpdate
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---- Diagnostics section ----
        Text(
            s.diagnostics.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassColors.textTertiary,
            modifier = Modifier.padding(start = 36.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassColors.glassBrush())
                .padding(vertical = 4.dp)
        ) {
            IOSListRow(
                title = "🔍 ${s.diagnostics}",
                subtitle = s.diagnosticsDesc
            )
            Divider()
            IOSListRow(
                title = "📄 ${s.exportDiagnostics}",
                subtitle = diagMessage,
                showChevron = true,
                onClick = onExportDiagnostics
            )
        }

        Spacer(Modifier.height(22.dp))

        // Language section — iOS grouped style
        Text(
            s.language.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassColors.textTertiary,
            modifier = Modifier.padding(start = 36.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassColors.glassBrush())
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                IOSSegmentedControl(
                    options = listOf(s.english, s.myanmar),
                    selectedIndex = if (language == "my") 1 else 0,
                    onSelect = { onLanguageChange(if (it == 1) "my" else "en") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // About section
        Text(
            s.about.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassColors.textTertiary,
            modifier = Modifier.padding(start = 36.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassColors.glassBrush())
                .padding(vertical = 4.dp)
        ) {
            IOSListRow(
                title = "GlassVPN",
                subtitle = "v$appVersion"
            )
            Divider()
            Text(
                s.aboutText,
                fontSize = 13.sp,
                color = GlassColors.textSecondary,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(22.dp))

        // Gaming tip
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassColors.glassBrush())
                .padding(vertical = 4.dp)
        ) {
            IOSListRow(
                title = "🎮",
                subtitle = if (language == "my")
                    "ဂိမ်းဆော့ဖို့ ping အနည်းဆုံး ဆာဗာကို ရွေးပါ။ မြန်မာကနေဆို စင်ကာပူ အမြန်ဆုံးပါ။"
                else
                    "For gaming, pick the lowest-ping server. From Myanmar, Singapore is fastest."
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 70.dp)
            .height(1.dp)
            .background(GlassColors.glassBorder.copy(alpha = 0.4f))
    )
}
