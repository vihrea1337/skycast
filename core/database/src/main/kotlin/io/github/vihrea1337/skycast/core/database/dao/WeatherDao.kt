package io.github.vihrea1337.skycast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Flow-методы НЕ suspend: Room сам эмитит при каждом изменении таблицы.
    // Nullable: кэша для города может ещё не быть.
    @Query("SELECT * FROM current_weather WHERE locationId = :locationId")
    fun observeCurrent(locationId: Long): Flow<CurrentWeatherEntity?>

    @Query("SELECT * FROM hourly_forecast WHERE locationId = :locationId ORDER BY time")
    fun observeHourly(locationId: Long): Flow<List<HourlyForecastEntity>>

    @Query("SELECT * FROM daily_forecast WHERE locationId = :locationId ORDER BY date")
    fun observeDaily(locationId: Long): Flow<List<DailyForecastEntity>>

    /** Прогноз заменяется атомарно: подписчики не увидят «полустарых» данных. */
    @Transaction
    suspend fun replaceForecast(
        locationId: Long,
        current: CurrentWeatherEntity,
        hourly: List<HourlyForecastEntity>,
        daily: List<DailyForecastEntity>,
    ) {
        upsertCurrent(current)
        deleteHourly(locationId)
        insertHourly(hourly)
        deleteDaily(locationId)
        insertDaily(daily)
    }

    @Upsert
    suspend fun upsertCurrent(current: CurrentWeatherEntity)

    @Insert
    suspend fun insertHourly(items: List<HourlyForecastEntity>)

    @Insert
    suspend fun insertDaily(items: List<DailyForecastEntity>)

    @Query("DELETE FROM hourly_forecast WHERE locationId = :locationId")
    suspend fun deleteHourly(locationId: Long)

    @Query("DELETE FROM daily_forecast WHERE locationId = :locationId")
    suspend fun deleteDaily(locationId: Long)
}
