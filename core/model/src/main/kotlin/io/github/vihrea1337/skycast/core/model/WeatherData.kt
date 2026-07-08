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
    /**
     * Единицы, в которых сервер отдал данные при синке. Могут отставать от текущих
     * настроек, если смена единиц случилась без сети — подписи в UI берутся отсюда,
     * чтобы не показывать «21» с подписью «°F» (docs/PLAN.md §5.12).
     */
    val temperatureUnit: TemperatureUnit,
    val windSpeedUnit: WindSpeedUnit,
)
