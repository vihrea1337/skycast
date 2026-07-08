package io.github.vihrea1337.skycast.core.model

data class UserSettings(
    val temperatureUnit: TemperatureUnit,
    val windSpeedUnit: WindSpeedUnit,
    val theme: ThemeMode,
)

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { METERS_PER_SECOND, KILOMETERS_PER_HOUR }

enum class ThemeMode { SYSTEM, LIGHT, DARK }
