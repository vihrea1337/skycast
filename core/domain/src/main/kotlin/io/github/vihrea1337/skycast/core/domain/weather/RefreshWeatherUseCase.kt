package io.github.vihrea1337.skycast.core.domain.weather

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import javax.inject.Inject

/**
 * Спам pull-to-refresh гасится флагом isRefreshing во ViewModel, а не лимитом здесь:
 * глобальный rate-limit ломал бы обновление при смене города и смене единиц.
 */
class RefreshWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = weatherRepository.refresh()
}
