package io.github.vihrea1337.skycast.core.model

data class Location(
    /** Идентификатор населённого пункта из geocoding API (GeoNames id). */
    val id: Long,
    val name: String,
    /** Регион (admin1), например «Карелия». Может отсутствовать. */
    val region: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)
