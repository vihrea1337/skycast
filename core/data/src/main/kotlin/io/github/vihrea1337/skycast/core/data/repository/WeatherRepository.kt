package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.WeatherData
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first контракт (docs/PLAN.md §5.7): Room — единственный источник правды.
 * UI никогда не читает из сети напрямую.
 */
interface WeatherRepository {

    /**
     * Погода выбранного города из локального кэша.
     * null — кэша ещё нет (первый запуск до успешного refresh).
     */
    fun observeWeather(): Flow<WeatherData?>

    /**
     * Сеть → Room. Данные придут реактивно через [observeWeather];
     * наружу — только успех/типизированная ошибка. Кэш при ошибке не трогается.
     */
    suspend fun refresh(): AppResult<Unit>
}
