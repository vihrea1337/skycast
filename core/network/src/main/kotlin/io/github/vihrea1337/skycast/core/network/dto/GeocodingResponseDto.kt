package io.github.vihrea1337.skycast.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    // При пустом результате поле results ОТСУТСТВУЕТ в JSON — дефолт обязателен
    @SerialName("results") val results: List<PlaceDto> = emptyList(),
)

@Serializable
data class PlaceDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("country") val country: String = "",
    @SerialName("admin1") val region: String? = null,
)
