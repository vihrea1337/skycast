package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import io.github.vihrea1337.skycast.core.network.dto.CurrentDto
import io.github.vihrea1337.skycast.core.network.dto.DailyDto
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.HourlyDto
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DefaultWeatherRepositoryTest {

    private val fixedInstant = Instant.parse("2026-07-08T09:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    @Test
    fun `successful response is mapped to domain weather`() = runTest {
        val repository = repository(FakeNetworkDataSource(forecastResult = AppResult.Success(sampleDto)))

        val result = repository.getWeather(petrozavodsk, UserSettings.Default)

        val data = (result as AppResult.Success).data
        assertEquals(WeatherType.OVERCAST, data.current.weatherType)
        assertEquals(fixedInstant, data.updatedAt)
    }

    @Test
    fun `network failure is passed through untouched`() = runTest {
        val repository = repository(FakeNetworkDataSource(forecastResult = AppResult.Failure(AppError.Network)))

        val result = repository.getWeather(petrozavodsk, UserSettings.Default)

        assertEquals(AppError.Network, (result as AppResult.Failure).error)
    }

    @Test
    fun `broken dto maps to unknown error instead of crash`() = runTest {
        val broken = sampleDto.copy(
            hourly = sampleDto.hourly.copy(temperature = emptyList()), // рассинхрон массивов
        )
        val repository = repository(FakeNetworkDataSource(forecastResult = AppResult.Success(broken)))

        val result = repository.getWeather(petrozavodsk, UserSettings.Default)

        assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
    }

    @Test
    fun `units from settings are forwarded to network`() = runTest {
        val fake = FakeNetworkDataSource(forecastResult = AppResult.Success(sampleDto))
        val settings = UserSettings.Default.copy(
            temperatureUnit = TemperatureUnit.FAHRENHEIT,
            windSpeedUnit = WindSpeedUnit.KILOMETERS_PER_HOUR,
        )

        repository(fake).getWeather(petrozavodsk, settings)

        assertEquals(TemperatureUnit.FAHRENHEIT, fake.lastTemperatureUnit)
        assertEquals(WindSpeedUnit.KILOMETERS_PER_HOUR, fake.lastWindSpeedUnit)
    }

    private fun kotlinx.coroutines.test.TestScope.repository(network: NetworkDataSource) =
        DefaultWeatherRepository(
            network = network,
            clock = fixedClock,
            defaultDispatcher = StandardTestDispatcher(testScheduler),
        )

    private class FakeNetworkDataSource(
        private val forecastResult: AppResult<ForecastResponseDto>,
    ) : NetworkDataSource {

        var lastTemperatureUnit: TemperatureUnit? = null
        var lastWindSpeedUnit: WindSpeedUnit? = null

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            temperatureUnit: TemperatureUnit,
            windSpeedUnit: WindSpeedUnit,
        ): AppResult<ForecastResponseDto> {
            lastTemperatureUnit = temperatureUnit
            lastWindSpeedUnit = windSpeedUnit
            return forecastResult
        }

        override suspend fun searchPlaces(query: String, language: String): AppResult<List<PlaceDto>> =
            AppResult.Success(emptyList())
    }

    private val petrozavodsk = Location(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
    )

    private val sampleDto = ForecastResponseDto(
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
            time = listOf("2026-07-08T14:00", "2026-07-08T15:00"),
            temperature = listOf(21.4, 22.1),
            weatherCode = listOf(3, 2),
            precipitationProbability = listOf(10, null),
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
