package io.github.vihrea1337.skycast.server

import io.github.vihrea1337.skycast.core.openmeteo.ForecastResponseDto
import java.time.Clock
import java.time.Instant

/** Координата вне допустимого диапазона — маппится в 400, см. Application.kt. */
class InvalidCoordinatesException(message: String) : IllegalArgumentException(message)

/**
 * Склеивает всё вместе: проверка координат → кэш → (если пусто) Open-Meteo → агрегация
 * параллельных массивов в список объектов → кладём в кэш → отдаём.
 */
class WeatherService(
    private val openMeteo: OpenMeteoClient,
    private val cache: WeatherCache,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun getWeather(latitude: Double, longitude: Double): AggregatedWeatherResponse {
        if (latitude !in MIN_LATITUDE..MAX_LATITUDE) {
            throw InvalidCoordinatesException("latitude вне диапазона [$MIN_LATITUDE, $MAX_LATITUDE]: $latitude")
        }
        if (longitude !in MIN_LONGITUDE..MAX_LONGITUDE) {
            throw InvalidCoordinatesException("longitude вне диапазона [$MIN_LONGITUDE, $MAX_LONGITUDE]: $longitude")
        }

        cache.get(latitude, longitude)?.let { return it.copy(cached = true) }

        val raw = openMeteo.getForecast(latitude, longitude)
        val aggregated = aggregate(latitude, longitude, raw)
        cache.put(latitude, longitude, aggregated)
        return aggregated
    }

    private fun aggregate(latitude: Double, longitude: Double, raw: ForecastResponseDto): AggregatedWeatherResponse {
        val hourly = raw.hourly.time.indices.map { i ->
            HourlyWeather(
                time = raw.hourly.time[i],
                temperatureC = raw.hourly.temperature[i],
                weatherCode = raw.hourly.weatherCode[i],
                precipitationProbabilityPercent = raw.hourly.precipitationProbability.getOrNull(i),
                isDay = raw.hourly.isDay.getOrNull(i)?.let { it == 1 } ?: true,
            )
        }
        val daily = raw.daily.time.indices.map { i ->
            DailyWeather(
                date = raw.daily.time[i],
                weatherCode = raw.daily.weatherCode[i],
                temperatureMaxC = raw.daily.temperatureMax[i],
                temperatureMinC = raw.daily.temperatureMin[i],
                sunrise = raw.daily.sunrise[i],
                sunset = raw.daily.sunset[i],
            )
        }
        return AggregatedWeatherResponse(
            latitude = latitude,
            longitude = longitude,
            current = CurrentWeather(
                time = raw.current.time,
                temperatureC = raw.current.temperature,
                feelsLikeC = raw.current.feelsLike,
                humidityPercent = raw.current.humidityPercent,
                weatherCode = raw.current.weatherCode,
                windSpeedKmh = raw.current.windSpeed,
                pressureHpa = raw.current.pressureHpa,
                isDay = raw.current.isDay == 1,
            ),
            hourly = hourly,
            daily = daily,
            generatedAt = Instant.now(clock).toString(),
            cached = false,
        )
    }

    private companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}
