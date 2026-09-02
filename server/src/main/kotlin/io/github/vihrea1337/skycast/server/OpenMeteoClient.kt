package io.github.vihrea1337.skycast.server

import io.github.vihrea1337.skycast.core.openmeteo.ForecastResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Ходит в Open-Meteo вместо клиента SkyCast. Поля запроса — те же, что в
 * `core/network/retrofit/ForecastApi.kt` (см. FORECAST_FIELDS ниже); единицы измерения
 * зафиксированы (цельсии, км/ч) — сервер не хранит пользовательские настройки, это его
 * собственный, независимый от приложения контракт.
 */
class OpenMeteoClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://api.open-meteo.com",
) {
    suspend fun getForecast(latitude: Double, longitude: Double): ForecastResponseDto =
        httpClient.get("$baseUrl/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("temperature_unit", "celsius")
            parameter("wind_speed_unit", "kmh")
            parameter("current", CURRENT_FIELDS)
            parameter("hourly", HOURLY_FIELDS)
            parameter("daily", DAILY_FIELDS)
            parameter("timezone", "auto")
            parameter("forecast_days", FORECAST_DAYS)
        }.body()

    companion object {
        private const val FORECAST_DAYS = 7
        private const val CURRENT_FIELDS = "temperature_2m,apparent_temperature,relative_humidity_2m," +
            "weather_code,wind_speed_10m,surface_pressure,is_day"
        private const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability,is_day"
        private const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset"
    }
}
