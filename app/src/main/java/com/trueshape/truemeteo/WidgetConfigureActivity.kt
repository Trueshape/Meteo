package com.trueshape.truemeteo

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/**
 * Si apre quando l'utente trascina il widget sulla home: permette di
 * scegliere quanto sfondo trasparente avere prima di confermare l'aggiunta.
 */
class WidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var valueLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se l'utente esce senza salvare, il sistema non deve aggiungere il widget.
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val startOpacity = WidgetPrefs.getOpacity(this, appWidgetId)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 96, 64, 64)

        val title = TextView(this)
        title.text = "Trasparenza sfondo del widget"
        title.textSize = 18f
        layout.addView(title)

        valueLabel = TextView(this)
        valueLabel.text = "Opacità: $startOpacity%"
        valueLabel.textSize = 15f
        valueLabel.setPadding(0, 32, 0, 16)
        layout.addView(valueLabel)

        val seekBar = SeekBar(this)
        seekBar.max = 100
        seekBar.progress = startOpacity
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueLabel.text = "Opacità: $progress%"
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        layout.addView(seekBar)

        val hint = TextView(this)
        hint.text = "0% = completamente trasparente, 100% = colore pieno."
        hint.textSize = 12f
        hint.setPadding(0, 8, 0, 32)
        layout.addView(hint)

        val saveButton = Button(this)
        saveButton.text = "Salva e aggiungi widget"
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL
        saveButton.layoutParams = buttonParams
        saveButton.setOnClickListener {
            WidgetPrefs.setOpacity(this, appWidgetId, seekBar.progress)

            val manager = AppWidgetManager.getInstance(this)
            WeatherWidgetProvider.updateWidget(this, manager, appWidgetId)

            val resultValue = android.content.Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
        layout.addView(saveButton)

        setContentView(layout)
    }
}
