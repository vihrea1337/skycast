package io.github.vihrea1337.skycast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settings: Flow<UserSettings> = dataStore.data
        // Повреждённый файл настроек — не повод крэшиться: даём дефолты
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserSettings(
                temperatureUnit = prefs[Keys.TEMPERATURE_UNIT]
                    .toEnumOrDefault(UserSettings.Default.temperatureUnit),
                windSpeedUnit = prefs[Keys.WIND_SPEED_UNIT]
                    .toEnumOrDefault(UserSettings.Default.windSpeedUnit),
                theme = prefs[Keys.THEME_MODE]
                    .toEnumOrDefault(UserSettings.Default.theme),
            )
        }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[Keys.TEMPERATURE_UNIT] = unit.name }
    }

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { it[Keys.WIND_SPEED_UNIT] = unit.name }
    }

    suspend fun setTheme(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}

/** Enum хранится строкой name; битое/устаревшее значение тихо падает в дефолт. */
private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
