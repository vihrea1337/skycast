package io.github.vihrea1337.skycast.core.network

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto
import io.github.vihrea1337.skycast.core.network.retrofit.ForecastApi
import io.github.vihrea1337.skycast.core.network.retrofit.GeocodingApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RetrofitNetworkDataSource @Inject constructor(
    private val forecastApi: ForecastApi,
    private val geocodingApi: GeocodingApi,
) : NetworkDataSource {

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit,
        windSpeedUnit: WindSpeedUnit,
    ): AppResult<ForecastResponseDto> = safeApiCall {
        forecastApi.getForecast(
            latitude = latitude,
            longitude = longitude,
            temperatureUnit = temperatureUnit.toQueryValue(),
            windSpeedUnit = windSpeedUnit.toQueryValue(),
        )
    }

    override suspend fun searchPlaces(query: String, language: String): AppResult<List<PlaceDto>> =
        safeApiCall {
            geocodingApi.search(name = query, language = language).results
        }
}

/** Единицы измерения конвертирует сервер — прокидываем их параметрами запроса (docs/PLAN.md §5.4). */
internal fun TemperatureUnit.toQueryValue(): String = when (this) {
    TemperatureUnit.CELSIUS -> "celsius"
    TemperatureUnit.FAHRENHEIT -> "fahrenheit"
}

internal fun WindSpeedUnit.toQueryValue(): String = when (this) {
    WindSpeedUnit.METERS_PER_SECOND -> "ms"
    WindSpeedUnit.KILOMETERS_PER_HOUR -> "kmh"
}
