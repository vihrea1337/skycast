package io.github.vihrea1337.skycast.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hourly_forecast",
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
data class HourlyForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    /** ISO local datetime («2026-07-08T15:00») — как отдаёт API при timezone=auto. */
    val time: String,
    val temperature: Double,
    val wmoCode: Int,
    val precipitationProbability: Int,
)
