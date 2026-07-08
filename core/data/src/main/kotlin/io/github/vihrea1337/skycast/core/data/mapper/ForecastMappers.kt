package io.github.vihrea1337.skycast.core.data.mapper

import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.DailyForecast
import io.github.vihrea1337.skycast.core.model.HourlyForecast
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.network.dto.CurrentDto
import io.github.vihrea1337.skycast.core.network.dto.DailyDto
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.HourlyDto
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal const val HOURS_IN_FORECAST = 24

/**
 * «Сшивает» параллельные массивы Open-Meteo в доменные модели.
 * Бросает исключение при рассинхроне массивов — репозиторий превращает его в AppError.Unknown.
 */
internal fun ForecastResponseDto.toWeatherData(location: Location, updatedAt: Instant): WeatherData {
    val currentTime = LocalDateTime.parse(current.time)
    return WeatherData(
        location = location,
        current = current.toCurrentWeather(),
        hourly = hourly.toHourlyForecasts(from = currentTime.truncatedTo(ChronoUnit.HOURS)),
        daily = daily.toDailyForecasts(),
        updatedAt = updatedAt,
    )
}

internal fun CurrentDto.toCurrentWeather(): CurrentWeather = CurrentWeather(
    temperature = temperature,
    feelsLike = feelsLike,
    humidityPercent = humidityPercent,
    pressureHpa = pressureHpa,
    windSpeed = windSpeed,
    weatherType = WeatherType.fromWmoCode(weatherCode),
    isDay = isDay == 1,
)

/** Срез: от текущего часа включительно, не более [HOURS_IN_FORECAST] элементов. */
internal fun HourlyDto.toHourlyForecasts(from: LocalDateTime): List<HourlyForecast> =
    time.indices
        .asSequence()
        .map { i ->
            HourlyForecast(
                time = LocalDateTime.parse(time[i]),
                temperature = temperature[i],
                weatherType = WeatherType.fromWmoCode(weatherCode[i]),
                precipitationProbability = precipitationProbability.getOrNull(i) ?: 0,
            )
        }
        .filter { it.time >= from }
        .take(HOURS_IN_FORECAST)
        .toList()

internal fun DailyDto.toDailyForecasts(): List<DailyForecast> =
    time.indices.map { i ->
        DailyForecast(
            date = LocalDate.parse(time[i]),
            tempMin = temperatureMin[i],
            tempMax = temperatureMax[i],
            weatherType = WeatherType.fromWmoCode(weatherCode[i]),
            sunrise = LocalDateTime.parse(sunrise[i]),
            sunset = LocalDateTime.parse(sunset[i]),
        )
    }
