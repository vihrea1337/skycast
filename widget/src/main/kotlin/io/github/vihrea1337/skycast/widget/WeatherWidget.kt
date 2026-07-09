package io.github.vihrea1337.skycast.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.domain.weather.ObserveWeatherUseCase
import io.github.vihrea1337.skycast.widget.ui.WidgetContent
import kotlinx.coroutines.flow.first

class WeatherWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Hilt в GlanceAppWidget не инжектится — зависимости через EntryPoint (docs/PLAN.md §5.13)
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        // Снапшот из Room: сеть здесь НЕ дёргаем; свежесть обеспечивают
        // WorkManager (периодика) и WidgetUpdater (после sync в приложении)
        val weather = entryPoint.observeWeather().invoke().first()
        provideContent {
            GlanceTheme {
                WidgetContent(weather = weather)
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun observeWeather(): ObserveWeatherUseCase
    }

    companion object {
        val SMALL = DpSize(110.dp, 50.dp)
        val MEDIUM = DpSize(220.dp, 120.dp)
    }
}
