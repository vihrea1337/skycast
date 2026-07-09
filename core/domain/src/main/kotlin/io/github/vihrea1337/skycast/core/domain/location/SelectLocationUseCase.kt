package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import javax.inject.Inject

/**
 * Только select: обновление погоды — реакция Home на смену выбранного города
 * (единая точка обработки ошибок refresh, переживает закрытие экрана поиска).
 */
class SelectLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(id: Long) = locationRepository.select(id)
}
