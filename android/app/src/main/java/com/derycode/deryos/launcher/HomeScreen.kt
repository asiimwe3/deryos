package com.derycode.deryos.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.derycode.deryos.ui.theme.DeryBlue
import com.derycode.deryos.ui.theme.DeryBlueBright
import com.derycode.deryos.ui.theme.DeryNavy
import com.derycode.deryos.ui.theme.DerySilver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A launcher entry: an installed app, or a built-in DeryOS surface.
 *
 * Icons are loaded as full-resolution bitmaps from PackageManager (192 px,
 * density-correct for the device), so tiles stay sharp on 1440p and 4K panels
 * instead of being upscaled from a small asset.
 */
data class LauncherEntry(
    val label: String,
    val packageName: String? = null,
    val vector: ImageVector? = null
)

private val SYSTEM_ENTRIES = listOf(
    LauncherEntry("Market", vector = Icons.Outlined.Storefront),
    LauncherEntry("DeryAI", vector = Icons.Outlined.AutoAwesome),
    LauncherEntry("Derycode ID", vector = Icons.Outlined.Person),
    LauncherEntry("Settings", vector = Icons.Outlined.Settings)
)

/** Read every launchable app on the device. */
private fun installedEntries(context: Context): List<LauncherEntry> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        .mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            LauncherEntry(info.loadLabel(pm).toString(), pkg)
        }
        .distinctBy { it.packageName }
        .filter { it.label.isNotBlank() }
        .sortedBy { it.label.lowercase() }
}

@Composable
fun HomeScreen(onOpenSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val entries = remember { installedEntries(context) }

    // Real app icons, decoded once and cached in memory.
    val icons = remember { mutableStateMapOf<String, ImageBitmap>() }
    LaunchedEffect(entries) {
        withContext(Dispatchers.IO) {
            entries.forEach { entry ->
                val pkg = entry.packageName ?: return@forEach
                val bitmap = runCatching {
                    context.packageManager.getApplicationIcon(pkg).toBitmap(192, 192)
                }.getOrNull()
                if (bitmap != null) {
                    withContext(Dispatchers.Main) { icons[pkg] = bitmap.asImageBitmap() }
                }
            }
        }
    }

    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    0f to DeryNavy.copy(alpha = 0.55f),
                    0.45f to DeryNavy,
                    1f to DeryNavy
                )
            )
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        ClockHeader(time = time, date = date)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(SYSTEM_ENTRIES) { entry ->
                AppTile(entry = entry, icon = null) {
                    if (entry.label == "Settings") onOpenSettings()
                    else android.widget.Toast
                        .makeText(context, entry.label + " arrives with the market", android.widget.Toast.LENGTH_SHORT)
                        .show()
                }
            }
            items(entries) { entry ->
                AppTile(entry = entry, icon = icons[entry.packageName]) {
                    entry.packageName?.let { pkg ->
                        context.packageManager.getLaunchIntentForPackage(pkg)?.let { launch ->
                            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(launch) }
                        }
                    }
                }
            }
        }

        Dock(icons = icons, entries = SYSTEM_ENTRIES)
    }
}

@Composable
private fun ClockHeader(time: String, date: String) {
    Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "DERYCODE",
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeryBlueBright
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "POWERED",
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Medium,
                color = DerySilver.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(time, fontSize = 62.sp, fontWeight = FontWeight.Thin, color = DerySilver, letterSpacing = (-2).sp)
        Text(date.uppercase(), fontSize = 11.sp, letterSpacing = 3.sp, color = DerySilver.copy(alpha = 0.6f))
    }
}

@Composable
private fun AppTile(
    entry: LauncherEntry,
    icon: ImageBitmap?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Brush.linearGradient(listOf(DeryBlue, DeryBlue.copy(alpha = 0.45f))))
                .border(1.dp, DerySilver.copy(alpha = 0.12f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                icon != null -> Image(
                    bitmap = icon,
                    contentDescription = entry.label,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(17.dp))
                )
                entry.vector != null -> Icon(
                    imageVector = entry.vector,
                    contentDescription = entry.label,
                    tint = DerySilver,
                    modifier = Modifier.size(26.dp)
                )
                else -> Text(
                    entry.label.take(1).uppercase(),
                    color = DerySilver,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry.label,
            fontSize = 11.sp,
            color = DerySilver.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Dock(icons: Map<String, ImageBitmap>, entries: List<LauncherEntry>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 14.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
            .clip(RoundedCornerShape(26.dp))
            .background(DeryNavy.copy(alpha = 0.72f))
            .border(1.dp, DerySilver.copy(alpha = 0.1f), RoundedCornerShape(26.dp))
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            entries.forEach { entry ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(DeryBlue.copy(alpha = 0.22f))
                        .border(1.dp, DerySilver.copy(alpha = 0.1f), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.vector != null) {
                        Icon(
                            imageVector = entry.vector,
                            contentDescription = entry.label,
                            tint = DeryBlueBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
