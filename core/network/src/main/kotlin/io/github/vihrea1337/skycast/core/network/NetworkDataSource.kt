package io.github.vihrea1337.skycast.core.network

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto

/** Публичный контракт модуля: Retrofit/OkHttp за ним не видны. */
interface NetworkDataSource {

    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit,
        windSpeedUnit: WindSpeedUnit,
    ): AppResult<ForecastResponseDto>

    suspend fun searchPlaces(query: String, language: String): AppResult<List<PlaceDto>>
}
