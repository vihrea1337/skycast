package io.github.vihrea1337.skycast.core.model

import java.time.LocalDateTime

data class HourlyForecast(
    /** Локальное время точки прогноза (API отдаёт его при timezone=auto). */
    val time: LocalDateTime,
    val temperature: Double,
    val weatherType: WeatherType,
    val precipitationProbability: Int,
)
