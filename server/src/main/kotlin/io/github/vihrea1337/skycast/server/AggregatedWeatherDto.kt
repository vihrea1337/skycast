package io.github.vihrea1337.skycast.server

import kotlinx.serialization.Serializable

/**
 * Наш собственный формат ответа `GET /api/v1/weather` — то, ради чего вообще нужен
 * этот сервер. Open-Meteo отдаёт hourly/daily как параллельные массивы (time[i],
 * temperature[i], ... относятся к одному моменту i) — их приходится сшивать руками
 * на клиенте. Здесь это уже сделано: один список объектов, каждый сам себе описание.
 */
@Serializable
data class AggregatedWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    /** Когда сервер собрал этот ответ (не путать с временем самого прогноза). */
    val generatedAt: String,
    /** true — отдано из кэша, false — только что сходили в Open-Meteo. */
    val cached: Boolean,
)

@Serializable
data class CurrentWeather(
    val time: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val pressureHpa: Double,
    val isDay: Boolean,
)

@Serializable
data class HourlyWeather(
    val time: String,
    val temperatureC: Double,
    val weatherCode: Int,
    val precipitationProbabilityPercent: Int?,
    val isDay: Boolean,
)

@Serializable
data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val temperatureMaxC: Double,
    val temperatureMinC: Double,
    val sunrise: String,
    val sunset: String,
)
