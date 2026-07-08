package io.github.vihrea1337.skycast.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.data.location.AndroidCurrentPositionProvider
import io.github.vihrea1337.skycast.core.data.location.CurrentPositionProvider
import io.github.vihrea1337.skycast.core.data.repository.DefaultLocationRepository
import io.github.vihrea1337.skycast.core.data.repository.DefaultSettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.DefaultWeatherRepository
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindsWeatherRepository(impl: DefaultWeatherRepository): WeatherRepository

    @Binds
    abstract fun bindsLocationRepository(impl: DefaultLocationRepository): LocationRepository

    @Binds
    abstract fun bindsSettingsRepository(impl: DefaultSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindsCurrentPositionProvider(impl: AndroidCurrentPositionProvider): CurrentPositionProvider

    companion object {
        /** Clock через DI — в тестах подменяется на Clock.fixed(...). */
        @Provides
        fun providesClock(): Clock = Clock.systemUTC()
    }
}
