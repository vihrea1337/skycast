package io.github.vihrea1337.skycast.server

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Кэш ответов в памяти процесса — намеренно самый простой вариант (без Redis/БД):
 * сервис учебный, переживать перезапуск ему не обязательно. Ключ — координаты,
 * округлённые до 0.01° (~1 км), чтобы близкие запросы (тот же город, разные пиксели
 * карты) попадали в одну запись вместо отдельного похода в Open-Meteo на каждую.
 */
class WeatherCache(
    private val ttl: Duration = Duration.ofMinutes(10),
    private val clock: Clock = Clock.systemUTC(),
) {
    private data class Entry(val value: AggregatedWeatherResponse, val expiresAt: Instant)

    private val entries = ConcurrentHashMap<String, Entry>()

    fun get(latitude: Double, longitude: Double): AggregatedWeatherResponse? {
        val entry = entries[key(latitude, longitude)] ?: return null
        if (Instant.now(clock).isAfter(entry.expiresAt)) return null
        return entry.value
    }

    fun put(latitude: Double, longitude: Double, value: AggregatedWeatherResponse) {
        entries[key(latitude, longitude)] = Entry(value, Instant.now(clock).plus(ttl))
    }

    private fun key(latitude: Double, longitude: Double): String {
        val rounded = { v: Double -> Math.round(v * 100) / 100.0 }
        return "${rounded(latitude)},${rounded(longitude)}"
    }
}
