package io.github.vihrea1337.skycast.core.testing

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/** Fake вместо mock (docs/PLAN.md §6.3): поведение в памяти, читается лучше моков. */
class FakeWeatherRepository : WeatherRepository {

    val weather = MutableStateFlow<WeatherData?>(null)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCount: Int = 0
        private set

    override fun observeWeather(): Flow<WeatherData?> = weather

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        return refreshResult
    }
}

class FakeLocationRepository : LocationRepository {

    private val locations = MutableStateFlow<List<Location>>(emptyList())
    private val selectedId = MutableStateFlow<Long?>(null)

    var searchResult: AppResult<List<Location>> = AppResult.Success(emptyList())
    var detectResult: AppResult<Location> = AppResult.Failure(AppError.Unknown)

    override fun observeSavedLocations(): Flow<List<Location>> = locations

    override fun observeSelected(): Flow<Location?> =
        combine(locations, selectedId) { list, id -> list.firstOrNull { it.id == id } }

    override suspend fun search(query: String): AppResult<List<Location>> = searchResult

    override suspend fun saveAndSelect(location: Location) {
        locations.update { list -> list.filterNot { it.id == location.id } + location }
        selectedId.value = location.id
    }

    override suspend fun select(id: Long) {
        selectedId.value = id
    }

    override suspend fun delete(id: Long) {
        locations.update { list -> list.filterNot { it.id == id } }
        if (selectedId.value == id) {
            selectedId.value = locations.value.firstOrNull()?.id
        }
    }

    override suspend fun detectCurrent(): AppResult<Location> = detectResult
}

class FakeSettingsRepository : SettingsRepository {

    private val state = MutableStateFlow(UserSettings.Default)

    override val settings: Flow<UserSettings> = state

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        state.update { it.copy(temperatureUnit = unit) }
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        state.update { it.copy(windSpeedUnit = unit) }
    }

    override suspend fun setTheme(mode: ThemeMode) {
        state.update { it.copy(theme = mode) }
    }
}
