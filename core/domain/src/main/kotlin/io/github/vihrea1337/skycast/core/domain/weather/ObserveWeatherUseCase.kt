package io.github.vihrea1337.skycast.core.domain.weather

import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import io.github.vihrea1337.skycast.core.model.WeatherData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Тонкий делегат (осознанно, docs/PLAN.md §5.8): ViewModel зависит от узкого
 * одноцелевого класса, а не от «толстого» репозитория — точка расширения на будущее.
 */
class ObserveWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(): Flow<WeatherData?> = weatherRepository.observeWeather()
}
