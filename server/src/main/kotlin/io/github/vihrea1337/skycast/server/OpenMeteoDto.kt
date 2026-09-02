package io.github.vihrea1337.skycast.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Сырой ответ Open-Meteo `/v1/forecast` — те же поля, что и в
 * `core/network/dto/ForecastResponseDto.kt` Android-приложения. Продублировано
 * намеренно: сервер живёт в отдельном (не-KMP) Gradle-модуле и не может напрямую
 * подключить Android-библиотеку. Если модели разойдутся — это заметит
 * [OpenMeteoClientTest], а полноценное решение (вынос в общий KMP-модуль) — уже
 * пройденный в проекте trade-off, см. docs/PLAN.md §4.2 (там же решили оставить
 * рукописные DTO вместо кодогенерации).
 */
@Serializable
data class OpenMeteoForecastDto(
    @SerialName("current") val current: OpenMeteoCurrentDto,
    @SerialName("hourly") val hourly: OpenMeteoHourlyDto,
    @SerialName("daily") val daily: OpenMeteoDailyDto,
)

@Serializable
data class OpenMeteoCurrentDto(
    @SerialName("time") val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val feelsLike: Double,
    @SerialName("relative_humidity_2m") val humidityPercent: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("surface_pressure") val pressureHpa: Double,
    @SerialName("is_day") val isDay: Int,
)

@Serializable
data class OpenMeteoHourlyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
    @SerialName("is_day") val isDay: List<Int> = emptyList(),
)

@Serializable
data class OpenMeteoDailyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerialName("sunrise") val sunrise: List<String>,
    @SerialName("sunset") val sunset: List<String>,
)
