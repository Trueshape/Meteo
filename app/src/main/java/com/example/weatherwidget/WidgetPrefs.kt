package com.example.weatherwidget

import android.content.Context

/** Preferenze per-widget (una entry per ogni istanza aggiunta alla home). */
object WidgetPrefs {

    private const val PREFS_NAME = "widget_prefs"
    private const val DEFAULT_OPACITY = 85

    /** "emoji" = emoji di sistema (il set originale), "vector" = disegni Weather Icons. */
    const val ICON_SET_EMOJI = "emoji"
    const val ICON_SET_VECTOR = "vector"
    private const val DEFAULT_ICON_SET = ICON_SET_EMOJI

    fun getOpacity(context: Context, appWidgetId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("opacity_$appWidgetId", DEFAULT_OPACITY)
    }

    fun setOpacity(context: Context, appWidgetId: Int, opacityPercent: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("opacity_$appWidgetId", opacityPercent).apply()
    }

    fun getIconSet(context: Context, appWidgetId: Int): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("icon_set_$appWidgetId", DEFAULT_ICON_SET) ?: DEFAULT_ICON_SET
    }

    fun setIconSet(context: Context, appWidgetId: Int, iconSet: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("icon_set_$appWidgetId", iconSet).apply()
    }

    fun remove(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("opacity_$appWidgetId").remove("icon_set_$appWidgetId").apply()
    }
}
