package io.github.vihrea1337.skycast.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vihrea1337.skycast.core.data.widget.WidgetPinner
import javax.inject.Inject

internal class GlanceWidgetPinner @Inject constructor(
    @ApplicationContext private val context: Context,
) : WidgetPinner {

    override val isPinSupported: Boolean
        get() = AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    override suspend fun requestPin(): Boolean =
        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(WeatherWidgetReceiver::class.java)
}
