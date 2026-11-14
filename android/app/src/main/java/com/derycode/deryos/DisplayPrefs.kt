package com.derycode.deryos

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Display presets for the launcher.
 *
 * A normal app cannot change the panel's real pixel count without system
 * privileges, so DeryOS does what it legitimately can: it re-renders its own
 * interface at a chosen density. Lower density shows more on screen and reads as
 * HD+, higher density gives larger, sharper elements and reads as QHD+ or 4K.
 */
object DisplayPrefs {
    const val STORE = "deryos.display"

    data class Preset(val key: String, val label: String, val detail: String, val dpi: Int)

    val presets = listOf(
        Preset("hd", "HD+", "Most content per screen, smallest elements", 260),
        Preset("fhd", "FHD+", "Balanced, recommended for most phones", 360),
        Preset("qhd", "QHD+", "Larger elements, premium spacing", 460),
        Preset("uhd", "4K / Max detail", "Biggest elements, fewest items per row", 560)
    )

    fun currentDpi(context: Context): Int =
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).getInt("dpi", 0)

    fun currentKey(context: Context): String =
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).getString("preset", "fhd") ?: "fhd"

    fun save(context: Context, preset: Preset) {
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit()
            .putInt("dpi", preset.dpi)
            .putString("preset", preset.key)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /** Walk the wrapper chain: LocalContext may be themed rather than the activity itself. */
    fun activityOf(context: Context): Activity? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return ctx as? Activity
    }

    fun columns(context: Context): Int =
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).getInt("columns", 0)
}
