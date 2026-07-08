package io.github.vihrea1337.skycast.core.model

data class UserSettings(
    val temperatureUnit: TemperatureUnit,
    val windSpeedUnit: WindSpeedUnit,
    val theme: ThemeMode,
) {
    companion object {
        /** Дефолты до появления экрана настроек (этап 4); останутся дефолтами DataStore. */
        val Default = UserSettings(
            temperatureUnit = TemperatureUnit.CELSIUS,
            windSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
            theme = ThemeMode.SYSTEM,
        )
    }
}

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { METERS_PER_SECOND, KILOMETERS_PER_HOUR }

enum class ThemeMode { SYSTEM, LIGHT, DARK }
