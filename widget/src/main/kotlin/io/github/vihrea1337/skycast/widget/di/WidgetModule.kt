package io.github.vihrea1337.skycast.widget.di

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.data.widget.WidgetUpdater
import io.github.vihrea1337.skycast.widget.WeatherWidget
import javax.inject.Inject

/** Реализация WidgetUpdater из :core:data — data-слой не знает про Glance. */
internal class GlanceWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
) : WidgetUpdater {

    override suspend fun updateAll() {
        WeatherWidget().updateAll(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WidgetModule {

    @Binds
    abstract fun bindsWidgetUpdater(impl: GlanceWidgetUpdater): WidgetUpdater
}
