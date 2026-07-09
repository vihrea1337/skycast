package io.github.vihrea1337.skycast.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    /** Появился первый виджет — включаем периодическое фоновое обновление. */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetSync.schedule(context)
    }

    /** Пользователь убрал последний виджет — фоновой работе больше незачем жить. */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetSync.cancel(context)
    }
}
