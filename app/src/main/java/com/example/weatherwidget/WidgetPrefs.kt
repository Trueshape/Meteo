package com.example.weatherwidget

import android.content.Context

/** Preferenze per-widget (una entry per ogni istanza aggiunta alla home). */
object WidgetPrefs {

    private const val PREFS_NAME = "widget_prefs"
    private const val DEFAULT_OPACITY = 85

    fun getOpacity(context: Context, appWidgetId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("opacity_$appWidgetId", DEFAULT_OPACITY)
    }

    fun setOpacity(context: Context, appWidgetId: Int, opacityPercent: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("opacity_$appWidgetId", opacityPercent).apply()
    }

    fun remove(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("opacity_$appWidgetId").apply()
    }
}
