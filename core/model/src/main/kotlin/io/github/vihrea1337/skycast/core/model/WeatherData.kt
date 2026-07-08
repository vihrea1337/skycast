package io.github.vihrea1337.skycast.core.model

import java.time.Instant

/** Агрегат «вся погода выбранного города» — то, что нужно Home-экрану и виджету. */
data class WeatherData(
    val location: Location,
    val current: CurrentWeather,
    /** Ближайшие 24 часа, начиная с текущего. */
    val hourly: List<HourlyForecast>,
    /** Прогноз на 7 дней, начиная с сегодняшнего. */
    val daily: List<DailyForecast>,
    /** Момент последней успешной синхронизации — для «обновлено N мин назад» и политики устаревания. */
    val updatedAt: Instant,
)
