package io.github.vihrea1337.skycast.core.data.mapper

import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.network.dto.CurrentDto
import io.github.vihrea1337.skycast.core.network.dto.DailyDto
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.HourlyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class ForecastMappersTest {

    @Test
    fun `parallel arrays are stitched by index`() {
        val hourly = HourlyDto(
            time = listOf("2026-07-08T14:00", "2026-07-08T15:00"),
            temperature = listOf(21.4, 22.1),
            weatherCode = listOf(3, 61),
            precipitationProbability = listOf(10, 55),
        )

        val result = hourly.toHourlyForecasts(from = LocalDateTime.parse("2026-07-08T14:00"))

        assertEquals(2, result.size)
        assertEquals(22.1, result[1].temperature, 0.0)
        assertEquals(WeatherType.RAIN, result[1].weatherType)
        assertEquals(55, result[1].precipitationProbability)
    }

    @Test
    fun `hours before current hour are dropped and slice is limited to 24`() {
        val times = (0 until 48).map { hour ->
            val day = if (hour < 24) "08" else "09"
            "2026-07-${day}T%02d:00".format(hour % 24)
        }
        val hourly = HourlyDto(
            time = times,
            temperature = List(48) { it.toDouble() },
            weatherCode = List(48) { 0 },
            precipitationProbability = List(48) { it },
        )

        val result = hourly.toHourlyForecasts(from = LocalDateTime.parse("2026-07-08T14:00"))

        assertEquals(HOURS_IN_FORECAST, result.size)
        assertEquals(LocalDateTime.parse("2026-07-08T14:00"), result.first().time)
        assertEquals(14.0, result.first().temperature, 0.0)
    }

    @Test
    fun `null precipitation probability degrades to zero`() {
        val hourly = HourlyDto(
            time = listOf("2026-07-08T14:00"),
            temperature = listOf(21.4),
            weatherCode = listOf(0),
            precipitationProbability = listOf(null),
        )

        val result = hourly.toHourlyForecasts(from = LocalDateTime.parse("2026-07-08T00:00"))

        assertEquals(0, result.single().precipitationProbability)
    }

    @Test
    fun `missing precipitation array degrades to zero`() {
        val hourly = HourlyDto(
            time = listOf("2026-07-08T14:00"),
            temperature = listOf(21.4),
            weatherCode = listOf(0),
        )

        assertEquals(0, hourly.toHourlyForecasts(from = LocalDateTime.MIN).single().precipitationProbability)
    }

    @Test
    fun `mismatched array lengths fail loudly instead of producing garbage`() {
        val broken = HourlyDto(
            time = listOf("2026-07-08T14:00", "2026-07-08T15:00"),
            temperature = listOf(21.4), // короче, чем time
            weatherCode = listOf(3, 2),
        )

        assertThrows(IndexOutOfBoundsException::class.java) {
            broken.toHourlyForecasts(from = LocalDateTime.MIN)
        }
    }

    @Test
    fun `full response maps current daily and metadata`() {
        val dto = ForecastResponseDto(
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
                weatherCode = listOf(61),
                temperatureMax = listOf(22.5),
                temperatureMin = listOf(14.1),
                sunrise = listOf("2026-07-08T02:57"),
                sunset = listOf("2026-07-08T22:12"),
            ),
        )
        val updatedAt = Instant.parse("2026-07-08T09:00:00Z")

        val data = dto.toWeatherData(location = petrozavodsk, updatedAt = updatedAt)

        assertEquals(petrozavodsk, data.location)
        assertEquals(WeatherType.OVERCAST, data.current.weatherType)
        assertTrue(data.current.isDay)
        assertEquals(WeatherType.RAIN, data.daily.single().weatherType)
        assertEquals(updatedAt, data.updatedAt)
    }

    private val petrozavodsk = Location(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
    )
}
