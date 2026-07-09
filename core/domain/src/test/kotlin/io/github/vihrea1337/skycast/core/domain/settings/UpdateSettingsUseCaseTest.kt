package io.github.vihrea1337.skycast.core.domain.settings

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSettingsUseCaseTest {

    private val calls = mutableListOf<String>()
    private val settingsRepository = RecordingSettingsRepository(calls)
    private val weatherRepository = RecordingWeatherRepository(calls)
    private val useCase = UpdateSettingsUseCase(settingsRepository, weatherRepository)

    @Test
    fun `temperature unit change writes setting first and then refreshes`() = runTest {
        useCase.temperatureUnit(TemperatureUnit.FAHRENHEIT)

        assertEquals(listOf("setTemperatureUnit", "refresh"), calls)
    }

    @Test
    fun `wind unit change writes setting first and then refreshes`() = runTest {
        useCase.windSpeedUnit(WindSpeedUnit.KILOMETERS_PER_HOUR)

        assertEquals(listOf("setWindSpeedUnit", "refresh"), calls)
    }

    @Test
    fun `theme change does not trigger refresh`() = runTest {
        useCase.theme(ThemeMode.DARK)

        assertEquals(listOf("setTheme"), calls)
    }

    private class RecordingSettingsRepository(
        private val calls: MutableList<String>,
    ) : SettingsRepository {

        override val settings: Flow<UserSettings> = MutableStateFlow(UserSettings.Default)

        override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
            calls += "setTemperatureUnit"
        }

        override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
            calls += "setWindSpeedUnit"
        }

        override suspend fun setTheme(mode: ThemeMode) {
            calls += "setTheme"
        }
    }

    private class RecordingWeatherRepository(
        private val calls: MutableList<String>,
    ) : WeatherRepository {

        override fun observeWeather(): Flow<WeatherData?> = flowOf(null)

        override suspend fun refresh(): AppResult<Unit> {
            calls += "refresh"
            return AppResult.Success(Unit)
        }
    }
}
