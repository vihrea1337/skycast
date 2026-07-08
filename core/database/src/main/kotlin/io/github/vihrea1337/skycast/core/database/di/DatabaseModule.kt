package io.github.vihrea1337.skycast.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.database.SkyCastDatabase
import io.github.vihrea1337.skycast.core.database.dao.LocationDao
import io.github.vihrea1337.skycast.core.database.dao.WeatherDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): SkyCastDatabase =
        Room.databaseBuilder(context, SkyCastDatabase::class.java, "skycast.db")
            // До v1.0 схема пляшет — потерять кэш погоды не страшно (docs/PLAN.md §5.5)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providesWeatherDao(database: SkyCastDatabase): WeatherDao = database.weatherDao()

    @Provides
    fun providesLocationDao(database: SkyCastDatabase): LocationDao = database.locationDao()
}
