package com.derycode.deryos

import android.content.Context
import android.content.res.Configuration
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
import com.derycode.deryos.settings.DisplaySettings
import com.derycode.deryos.ui.theme.DeryOSTheme

/**
 * The DeryOS home activity.
 *
 * Registered with CATEGORY_HOME, so Android treats it as a home screen
 * replacement and offers it when the user presses Home.
 */
class MainActivity : ComponentActivity() {

    /**
     * Apply the saved display preset before any resource is resolved, so the
     * chosen density is used for the whole interface, not just new views.
     */
    override fun attachBaseContext(newBase: Context) {
        val dpi = DisplayPrefs.currentDpi(newBase)
        if (dpi > 0) {
            val config = Configuration(newBase.resources.configuration)
            config.densityDpi = dpi
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DeryOSApp() }
    }
}

@Composable
private fun DeryOSApp() {
    var booted by remember { mutableStateOf(false) }
    var inSettings by remember { mutableStateOf(false) }

    DeryOSTheme {
        when {
            !booted -> BootScreen(onFinished = { booted = true })
            inSettings -> DisplaySettings(onBack = { inSettings = false })
            else -> HomeScreen(onOpenSettings = { inSettings = true })
        }
    }
}
