package com.derycode.deryos.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.derycode.deryos.DisplayPrefs
import com.derycode.deryos.ui.theme.DeryBlue
import com.derycode.deryos.ui.theme.DeryBlueBright
import com.derycode.deryos.ui.theme.DeryNavy
import com.derycode.deryos.ui.theme.DerySilver

/**
 * Display settings: the resolution preset switch.
 *
 * Changing a preset stores a density and recreates the activity, which is the
 * only way an ordinary app can honestly change how much it renders per screen.
 * The dialog says so instead of pretending to change the panel itself.
 */
@Composable
fun DisplaySettings(onBack: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(DisplayPrefs.currentKey(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeryNavy, DeryNavy)))
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 22.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeryBlue.copy(alpha = 0.16f))
                    .border(1.dp, DerySilver.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = DerySilver,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Text("Display", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = DerySilver)
        }

        Text(
            "SCREEN RESOLUTION",
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            color = DerySilver.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(10.dp))

        DisplayPrefs.presets.forEach { preset ->
            val active = preset.key == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) DeryBlue.copy(alpha = 0.18f) else DeryNavy.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (active) DeryBlueBright.copy(alpha = 0.55f) else DerySilver.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        selected = preset.key
                        DisplayPrefs.save(context, preset)
                        // recreate() re-resolves resources at the new density
                        DisplayPrefs.activityOf(context)?.recreate()
                    }
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(preset.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DerySilver)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${preset.detail} · ${preset.dpi} dpi",
                        fontSize = 11.sp,
                        color = DerySilver.copy(alpha = 0.55f)
                    )
                }
                if (active) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = DeryBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DeryBlue.copy(alpha = 0.1f))
                .border(1.dp, DerySilver.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(15.dp)
        ) {
            Text(
                "DeryOS redraws its own interface at the density you pick, so icons, text and spacing rescale immediately. " +
                    "Changing the panel's true pixel count needs system privileges, which arrive with the AOSP build in Phase 5.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = DerySilver.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DerySilver.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .clickable {
                    DisplayPrefs.reset(context)
                    DisplayPrefs.activityOf(context)?.recreate()
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Use the device default", fontSize = 13.sp, color = DerySilver.copy(alpha = 0.85f))
        }

        Spacer(Modifier.height(26.dp))
    }
}
