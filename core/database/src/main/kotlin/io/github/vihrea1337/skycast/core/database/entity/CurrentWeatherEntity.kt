package io.github.vihrea1337.skycast.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_weather",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CurrentWeatherEntity(
    @PrimaryKey val locationId: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val pressureHpa: Double,
    val windSpeed: Double,
    val wmoCode: Int,
    val isDay: Boolean,
    /** Момент синка (epoch seconds) — для «обновлено N мин назад» и политики устаревания. */
    val updatedAtEpochSec: Long,
    /**
     * Единицы, в которых сервер отдал данные при синке (enum.name).
     * Смена единиц в настройках без сети не должна давать «21» с подписью «°F» (docs/PLAN.md §5.12).
     */
    val temperatureUnit: String,
    val windSpeedUnit: String,
)
