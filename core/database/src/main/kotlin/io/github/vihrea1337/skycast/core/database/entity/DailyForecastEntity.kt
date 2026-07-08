package io.github.vihrea1337.skycast.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_forecast",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("locationId")],
)
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    /** ISO date («2026-07-08»). */
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val wmoCode: Int,
    val sunrise: String,
    val sunset: String,
)
