package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.testdouble.FakeLocationDao
import io.github.vihrea1337.skycast.core.data.testdouble.FakeWeatherDao
import io.github.vihrea1337.skycast.core.data.widget.WidgetUpdater
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import io.github.vihrea1337.skycast.core.network.dto.CurrentDto
import io.github.vihrea1337.skycast.core.network.dto.DailyDto
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.HourlyDto
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DefaultWeatherRepositoryTest {

    private val fixedInstant = Instant.parse("2026-07-08T09:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    private val weatherDao = FakeWeatherDao()
    private val locationDao = FakeLocationDao()
    private val widgetUpdater = RecordingWidgetUpdater()

    @Test
    fun `refresh without selected location is a no-op success`() = runTest {
        val network = FakeNetworkDataSource(AppResult.Success(sampleDto()))
        val repository = repository(network)

        val result = repository.refresh()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(0, network.forecastCallCount)
        assertNull(repository.observeWeather().first())
    }

    @Test
    fun `refresh fills cache and observeWeather emits mapped weather`() = runTest {
        locationDao.saveAndSelect(petrozavodsk)
        val repository = repository(FakeNetworkDataSource(AppResult.Success(sampleDto())))

        val result = repository.refresh()

        assertEquals(AppResult.Success(Unit), result)
        val weather = repository.observeWeather().first()!!
        assertEquals("Петрозаводск", weather.location.name)
        assertEquals(WeatherType.OVERCAST, weather.current.weatherType)
        assertEquals(fixedInstant, weather.updatedAt)
        assertEquals(TemperatureUnit.CELSIUS, weather.temperatureUnit)
        assertEquals(1, widgetUpdater.updateCount)
    }

    @Test
    fun `refresh failure keeps existing cache untouched`() = runTest {
        locationDao.saveAndSelect(petrozavodsk)
        val network = FakeNetworkDataSource(AppResult.Success(sampleDto(temperature = 10.0)))
        val repository = repository(network)
        repository.refresh()

        network.forecastResult = AppResult.Failure(AppError.Network)
        widgetUpdater.updateCount = 0
        val result = repository.refresh()

        assertEquals(AppError.Network, (result as AppResult.Failure).error)
        assertEquals(10.0, repository.observeWeather().first()!!.current.temperature, 0.0)
        assertEquals(0, widgetUpdater.updateCount) // виджет не трогаем при неудаче
    }

    @Test
    fun `broken dto maps to unknown error and keeps cache`() = runTest {
        locationDao.saveAndSelect(petrozavodsk)
        val network = FakeNetworkDataSource(AppResult.Success(sampleDto(temperature = 10.0)))
        val repository = repository(network)
        repository.refresh()

        network.forecastResult = AppResult.Success(
            sampleDto().let { it.copy(hourly = it.hourly.copy(temperature = emptyList())) },
        )
        val result = repository.refresh()

        assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
        assertEquals(10.0, repository.observeWeather().first()!!.current.temperature, 0.0)
    }

    @Test
    fun `switching selected location switches weather stream`() = runTest {
        locationDao.saveAndSelect(petrozavodsk)
        val repository = repository(FakeNetworkDataSource(AppResult.Success(sampleDto(temperature = 21.0))))
        repository.refresh()

        val moscow = LocationEntity(
            id = 524901,
            name = "Москва",
            region = null,
            country = "Россия",
            latitude = 55.75,
            longitude = 37.62,
            isSelected = false,
            sortOrder = 1,
        )
        locationDao.upsert(moscow)
        locationDao.select(moscow.id)

        // у Москвы кэша ещё нет — flatMapLatest должен переключиться на пустой поток
        assertNull(repository.observeWeather().first())

        locationDao.select(petrozavodsk.id)
        assertEquals(21.0, repository.observeWeather().first()!!.current.temperature, 0.0)
    }

    private fun TestScope.repository(network: NetworkDataSource) = DefaultWeatherRepository(
        network = network,
        weatherDao = weatherDao,
        locationDao = locationDao,
        settingsRepository = FakeSettingsRepository(),
        widgetUpdater = widgetUpdater,
        clock = fixedClock,
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private class RecordingWidgetUpdater : WidgetUpdater {
        var updateCount = 0
        override suspend fun updateAll() {
            updateCount++
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val settings: Flow<UserSettings> = MutableStateFlow(UserSettings.Default)
        override suspend fun setTemperatureUnit(unit: TemperatureUnit) = Unit
        override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) = Unit
        override suspend fun setTheme(mode: ThemeMode) = Unit
    }

    private class FakeNetworkDataSource(
        var forecastResult: AppResult<ForecastResponseDto>,
    ) : NetworkDataSource {

        var forecastCallCount = 0
            private set

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            temperatureUnit: TemperatureUnit,
            windSpeedUnit: WindSpeedUnit,
        ): AppResult<ForecastResponseDto> {
            forecastCallCount++
            return forecastResult
        }

        override suspend fun searchPlaces(query: String, language: String): AppResult<List<PlaceDto>> =
            AppResult.Success(emptyList())
    }

    private val petrozavodsk = LocationEntity(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
        isSelected = true,
        sortOrder = 0,
    )

    private fun sampleDto(temperature: Double = 21.4) = ForecastResponseDto(
        current = CurrentDto(
            time = "2026-07-08T14:00",
            temperature = temperature,
            feelsLike = temperature - 1.6,
            humidityPercent = 62,
            weatherCode = 3,
            windSpeed = 4.2,
            pressureHpa = 1006.9,
            isDay = 1,
        ),
        hourly = HourlyDto(
            time = listOf("2026-07-08T14:00", "2026-07-08T15:00"),
            temperature = listOf(temperature, temperature + 0.7),
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
