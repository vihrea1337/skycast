package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.datastore.SettingsDataSource
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Тонкая обёртка над DataStore: UI не знает про механизм хранения. */
interface SettingsRepository {

    val settings: Flow<UserSettings>

    suspend fun setTemperatureUnit(unit: TemperatureUnit)
    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)
    suspend fun setTheme(mode: ThemeMode)
}

internal class DefaultSettingsRepository @Inject constructor(
    private val dataSource: SettingsDataSource,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataSource.settings

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = dataSource.setTemperatureUnit(unit)

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) = dataSource.setWindSpeedUnit(unit)

    override suspend fun setTheme(mode: ThemeMode) = dataSource.setTheme(mode)
}
