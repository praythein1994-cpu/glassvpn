package com.glassvpn.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun SettingsScreen(
    s: AppStrings,
    language: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            s.settings,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = GlassColors.textPrimary,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
        )

        Spacer(Modifier.height(8.dp))

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
                subtitle = "v1.0"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp)
                    .height(1.dp)
                    .background(GlassColors.glassBorder.copy(alpha = 0.4f))
            )
            IOSListRow(
                title = s.about,
                subtitle = null
            )
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
    }
}
