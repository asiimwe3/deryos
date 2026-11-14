package com.derycode.deryos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.derycode.deryos.launcher.HomeScreen
import com.derycode.deryos.ui.theme.DeryOSTheme

/**
 * The DeryOS home activity.
 *
 * Registered with CATEGORY_HOME, so Android treats it as a home screen
 * replacement and offers it when the user presses Home.
 *
 * Phase 2 keeps this as a normal application. Phase 5 replaces this activity
 * with the AOSP Launcher3 fork inside a custom system image.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DeryOSApp() }
    }
}

@Composable
private fun DeryOSApp() {
    var booted by remember { mutableStateOf(false) }
    DeryOSTheme {
        if (!booted) {
            BootScreen(onFinished = { booted = true })
        } else {
            HomeScreen()
        }
    }
}
