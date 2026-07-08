package io.github.vihrea1337.skycast.core.data.mapper

import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto

internal fun PlaceDto.toLocation(): Location = Location(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = latitude,
    longitude = longitude,
)

internal fun Location.toEntity(isSelected: Boolean = false, sortOrder: Int = 0): LocationEntity =
    LocationEntity(
        id = id,
        name = name,
        region = region,
        country = country,
        latitude = latitude,
        longitude = longitude,
        isSelected = isSelected,
        sortOrder = sortOrder,
    )
