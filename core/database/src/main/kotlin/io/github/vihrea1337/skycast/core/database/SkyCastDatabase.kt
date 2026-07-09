package io.github.vihrea1337.skycast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.vihrea1337.skycast.core.database.dao.LocationDao
import io.github.vihrea1337.skycast.core.database.dao.WeatherDao
import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity

/**
 * Пока схема активно меняется (этапы 3–5): exportSchema = false +
 * destructive migrations. Перед v1.0 включаем exportSchema, кладём schemas/
 * в git и переходим на настоящие миграции (docs/PLAN.md §5.5).
 */
@Database(
    entities = [
        LocationEntity::class,
        CurrentWeatherEntity::class,
        HourlyForecastEntity::class,
        DailyForecastEntity::class,
    ],
    version = 2, // v2: hourly_forecast.isDay
    exportSchema = false,
)
internal abstract class SkyCastDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun locationDao(): LocationDao
}
