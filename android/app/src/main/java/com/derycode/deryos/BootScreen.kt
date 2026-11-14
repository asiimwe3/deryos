package com.derycode.deryos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.derycode.deryos.ui.theme.DeryBlue
import com.derycode.deryos.ui.theme.DeryNavy
import com.derycode.deryos.ui.theme.DerySilver
import com.derycode.deryos.ui.theme.DeryBlueBright
import kotlinx.coroutines.delay

/** Derycode boot animation concept: mark, wordmark, progress line. */
@Composable
fun BootScreen(onFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(targetValue = progress, animationSpec = tween(1800), label = "boot")

    LaunchedEffect(Unit) {
        progress = 1f
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(DeryNavy, DeryNavy.copy(alpha = 0.98f)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(DeryBlue, DeryBlueBright))),
                contentAlignment = Alignment.Center
            ) {
                Text("D", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = DerySilver)
            }
            Spacer(Modifier.height(22.dp))
            Text("DERYCODE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, color = DerySilver)
            Spacer(Modifier.height(8.dp))
            Text("POWERED", fontSize = 11.sp, letterSpacing = 6.sp, color = DerySilver.copy(alpha = 0.7f))
            Spacer(Modifier.height(30.dp))
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DerySilver.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .width(140.dp * animated)
                        .height(3.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.horizontalGradient(listOf(DeryBlue, DeryBlueBright)))
                )
            }
        }
    }
}
