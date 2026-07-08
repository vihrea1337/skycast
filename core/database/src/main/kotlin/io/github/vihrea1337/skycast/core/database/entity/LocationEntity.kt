package io.github.vihrea1337.skycast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    /** GeoNames id из geocoding API — стабильный внешний идентификатор. */
    @PrimaryKey val id: Long,
    val name: String,
    val region: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    /** Ровно один город выбран; инвариант поддерживает LocationDao.select(). */
    val isSelected: Boolean,
    val sortOrder: Int,
)
