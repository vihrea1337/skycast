package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData

/**
 * Этап 2: прямой запрос в сеть без кэша.
 * На этапе 3 контракт станет offline-first: observeWeather(): Flow из Room + refresh()
 * (docs/PLAN.md §5.7) — вызывающий код поменяется минимально.
 */
interface WeatherRepository {

    suspend fun getWeather(location: Location, settings: UserSettings): AppResult<WeatherData>
}
