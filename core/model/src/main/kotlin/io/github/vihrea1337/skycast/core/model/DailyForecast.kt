package io.github.vihrea1337.skycast.core.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyForecast(
    val date: LocalDate,
    val tempMin: Double,
    val tempMax: Double,
    val weatherType: WeatherType,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
)
