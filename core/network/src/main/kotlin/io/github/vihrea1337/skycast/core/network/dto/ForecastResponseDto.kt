package io.github.vihrea1337.skycast.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Контракт ответа /v1/forecast — см. openapi/weather-api.yaml в корне репозитория. */
@Serializable
data class ForecastResponseDto(
    @SerialName("current") val current: CurrentDto,
    @SerialName("hourly") val hourly: HourlyDto,
    @SerialName("daily") val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    @SerialName("time") val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val feelsLike: Double,
    @SerialName("relative_humidity_2m") val humidityPercent: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("surface_pressure") val pressureHpa: Double,
    @SerialName("is_day") val isDay: Int,
)

/** Параллельные массивы: элемент i всех списков относится к моменту time[i]. */
@Serializable
data class HourlyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    // Вероятность осадков может отсутствовать или содержать null-элементы
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
    // День/ночь для каждого часа — влияет на иконку (солнце/луна)
    @SerialName("is_day") val isDay: List<Int> = emptyList(),
)

@Serializable
data class DailyDto(
    @SerialName("time") val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerialName("sunrise") val sunrise: List<String>,
    @SerialName("sunset") val sunset: List<String>,
)
