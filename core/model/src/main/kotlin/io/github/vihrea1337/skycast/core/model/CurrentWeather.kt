package io.github.vihrea1337.skycast.core.model

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val pressureHpa: Double,
    val windSpeed: Double,
    val weatherType: WeatherType,
    /** День или ночь в точке наблюдения — влияет на иконку (солнце/луна). */
    val isDay: Boolean,
)
