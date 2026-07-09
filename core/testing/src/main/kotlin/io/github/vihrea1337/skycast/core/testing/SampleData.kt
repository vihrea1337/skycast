package io.github.vihrea1337.skycast.core.testing

import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import java.time.Instant

fun sampleLocation(
    id: Long = 509820,
    name: String = "Петрозаводск",
): Location = Location(
    id = id,
    name = name,
    region = "Карелия",
    country = "Россия",
    latitude = 61.78491,
    longitude = 34.34691,
)

fun sampleWeather(
    location: Location = sampleLocation(),
    temperature: Double = 21.4,
    updatedAt: Instant = Instant.parse("2026-07-09T12:00:00Z"),
): WeatherData = WeatherData(
    location = location,
    current = CurrentWeather(
        temperature = temperature,
        feelsLike = temperature - 1.6,
        humidityPercent = 62,
        pressureHpa = 1006.9,
        windSpeed = 4.2,
        weatherType = WeatherType.PARTLY_CLOUDY,
        isDay = true,
    ),
    hourly = emptyList(),
    daily = emptyList(),
    updatedAt = updatedAt,
    temperatureUnit = TemperatureUnit.CELSIUS,
    windSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
)
