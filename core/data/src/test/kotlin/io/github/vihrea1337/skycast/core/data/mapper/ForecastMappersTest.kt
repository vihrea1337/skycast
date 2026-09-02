package io.github.vihrea1337.skycast.core.data.mapper

import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.openmeteo.CurrentDto
import io.github.vihrea1337.skycast.core.openmeteo.DailyDto
import io.github.vihrea1337.skycast.core.openmeteo.ForecastResponseDto
import io.github.vihrea1337.skycast.core.openmeteo.HourlyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class ForecastMappersTest {

    // ---------- DTO → Entity ----------

    @Test
    fun `hourly slice starts at current hour and is limited to 24`() {
        val hourly = HourlyDto(
            time = (0 until 48).map { hour ->
                val day = if (hour < 24) "08" else "09"
                "2026-07-${day}T%02d:00".format(hour % 24)
            },
            temperature = List(48) { it.toDouble() },
            weatherCode = List(48) { 0 },
            precipitationProbability = List(48) { it },
        )

        val result = hourly.toHourlyForecastEntities(locationId = 1, currentTime = "2026-07-08T14:25")

        assertEquals(HOURS_IN_FORECAST, result.size)
        assertEquals("2026-07-08T14:00", result.first().time) // усечение к началу часа
        assertEquals(14.0, result.first().temperature, 0.0)
    }

    @Test
    fun `missing is_day array degrades to day icon`() {
        val hourly = HourlyDto(
            time = listOf("2026-07-08T14:00"),
            temperature = listOf(21.4),
            weatherCode = listOf(0),
        )

        assertTrue(hourly.toHourlyForecastEntities(1, "2026-07-08T00:00").single().isDay)
    }

    @Test
    fun `null and missing precipitation degrade to zero`() {
        val withNull = HourlyDto(
            time = listOf("2026-07-08T14:00"),
            temperature = listOf(21.4),
            weatherCode = listOf(0),
            precipitationProbability = listOf(null),
        )
        val withoutArray = withNull.copy(precipitationProbability = emptyList())

        assertEquals(0, withNull.toHourlyForecastEntities(1, "2026-07-08T00:00").single().precipitationProbability)
        assertEquals(0, withoutArray.toHourlyForecastEntities(1, "2026-07-08T00:00").single().precipitationProbability)
    }

    @Test
    fun `mismatched array lengths fail loudly instead of producing garbage`() {
        val broken = HourlyDto(
            time = listOf("2026-07-08T14:00", "2026-07-08T15:00"),
            temperature = listOf(21.4), // короче, чем time
            weatherCode = listOf(3, 2),
        )

        assertThrows(IndexOutOfBoundsException::class.java) {
            broken.toHourlyForecastEntities(locationId = 1, currentTime = "2026-07-08T00:00")
        }
    }

    @Test
    fun `current dto keeps sync units and timestamp`() {
        val dto = sampleDto()
        val updatedAt = Instant.parse("2026-07-08T09:00:00Z")

        val entity = dto.toCurrentWeatherEntity(locationId = 1, settings = UserSettings.Default, updatedAt = updatedAt)

        assertEquals(updatedAt.epochSecond, entity.updatedAtEpochSec)
        assertEquals("CELSIUS", entity.temperatureUnit)
        assertEquals("METERS_PER_SECOND", entity.windSpeedUnit)
        assertTrue(entity.isDay)
    }

    // ---------- Entity → домен ----------

    @Test
    fun `entities map to domain weather with wmo decoding`() {
        val location = LocationEntity(
            id = 509820,
            name = "Петрозаводск",
            region = "Карелия",
            country = "Россия",
            latitude = 61.78491,
            longitude = 34.34691,
            isSelected = true,
            sortOrder = 0,
        )
        val current = CurrentWeatherEntity(
            locationId = 509820,
            temperature = 21.4,
            feelsLike = 19.8,
            humidityPercent = 62,
            pressureHpa = 1006.9,
            windSpeed = 4.2,
            wmoCode = 95,
            isDay = false,
            updatedAtEpochSec = 1_784_000_000,
            temperatureUnit = "CELSIUS",
            windSpeedUnit = "METERS_PER_SECOND",
        )
        val hourly = listOf(
            HourlyForecastEntity(
                locationId = 509820,
                time = "2026-07-08T15:00",
                temperature = 22.1,
                wmoCode = 61,
                precipitationProbability = 55,
                isDay = false,
            ),
        )
        val daily = listOf(
            DailyForecastEntity(
                locationId = 509820,
                date = "2026-07-08",
                tempMin = 14.1,
                tempMax = 22.5,
                wmoCode = 71,
                sunrise = "2026-07-08T02:57",
                sunset = "2026-07-08T22:12",
            ),
        )

        val weather = toWeatherData(location, current, hourly, daily)

        assertEquals("Петрозаводск", weather.location.name)
        assertEquals(WeatherType.THUNDERSTORM, weather.current.weatherType)
        assertEquals(WeatherType.RAIN, weather.hourly.single().weatherType)
        assertEquals(LocalDateTime.parse("2026-07-08T15:00"), weather.hourly.single().time)
        assertEquals(false, weather.hourly.single().isDay)
        assertEquals(WeatherType.SNOW, weather.daily.single().weatherType)
        assertEquals(Instant.ofEpochSecond(1_784_000_000), weather.updatedAt)
    }

    private fun sampleDto() = ForecastResponseDto(
        current = CurrentDto(
            time = "2026-07-08T14:00",
            temperature = 21.4,
            feelsLike = 19.8,
            humidityPercent = 62,
            weatherCode = 3,
            windSpeed = 4.2,
            pressureHpa = 1006.9,
            isDay = 1,
        ),
        hourly = HourlyDto(
            time = listOf("2026-07-08T14:00"),
            temperature = listOf(21.4),
            weatherCode = listOf(3),
        ),
        daily = DailyDto(
            time = listOf("2026-07-08"),
            weatherCode = listOf(3),
            temperatureMax = listOf(22.5),
            temperatureMin = listOf(14.1),
            sunrise = listOf("2026-07-08T02:57"),
            sunset = listOf("2026-07-08T22:12"),
        ),
    )
}
