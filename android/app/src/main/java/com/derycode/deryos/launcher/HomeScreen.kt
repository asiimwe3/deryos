package com.derycode.deryos.launcher

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.derycode.deryos.ui.theme.DeryBlue
import com.derycode.deryos.ui.theme.DeryBlueBright
import com.derycode.deryos.ui.theme.DeryNavy
import com.derycode.deryos.ui.theme.DerySilver

/** A launcher entry: an installed app or a DeryOS surface. */
data class LauncherEntry(val label: String, val packageName: String? = null, val glyph: String)

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Real installed apps, read through PackageManager. This is what makes the
    // prototype an actual launcher rather than a mockup.
    val installed = remember {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
            .distinctBy { it.second }
            .sortedBy { it.first.lowercase() }
            .map { (label, pkg) -> LauncherEntry(label, pkg, label.take(1).uppercase()) }
    }

    val derycode = listOf(
        LauncherEntry("App Market", null, "S"),
        LauncherEntry("DeryAI", null, "AI"),
        LauncherEntry("Derycode ID", null, "ID"),
        LauncherEntry("Settings", null, "G")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(DeryNavy, DeryNavy.copy(alpha = 0.96f))))
    ) {
        ClockWidget()
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(derycode) { entry ->
                AppTile(entry) { }
            }
            items(installed) { entry ->
                AppTile(entry) {
                    entry.packageName?.let { pkg ->
                        context.packageManager.getLaunchIntentForPackage(pkg)?.let { launch ->
                            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launch)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockWidget() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 34.dp, bottom = 12.dp)) {
        Text("DeryOS", fontSize = 12.sp, letterSpacing = 3.sp, color = DeryBlueBright, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Prototype build", fontSize = 30.sp, fontWeight = FontWeight.Light, color = DerySilver)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun AppTile(entry: LauncherEntry, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(DeryBlue, DeryBlue.copy(alpha = 0.55f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.glyph, color = DerySilver, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(7.dp))
        Text(entry.label, fontSize = 10.sp, color = DerySilver.copy(alpha = 0.85f), maxLines = 1)
    }
}
