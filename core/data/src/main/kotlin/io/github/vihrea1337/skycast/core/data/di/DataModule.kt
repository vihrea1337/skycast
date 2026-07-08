package io.github.vihrea1337.skycast.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.data.repository.DefaultWeatherRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindsWeatherRepository(impl: DefaultWeatherRepository): WeatherRepository

    companion object {
        /** Clock через DI — в тестах подменяется на Clock.fixed(...). */
        @Provides
        fun providesClock(): Clock = Clock.systemUTC()
    }
}
