package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.model.Location
import javax.inject.Inject

/**
 * Композитный сценарий (ради таких слой и существует, docs/PLAN.md §5.8):
 * определить город по геолокации → сохранить → выбрать.
 * При ошибке определения ничего не сохраняется.
 */
class DetectMyLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): AppResult<Location> =
        when (val detected = locationRepository.detectCurrent()) {
            is AppResult.Failure -> detected
            is AppResult.Success -> {
                locationRepository.saveAndSelect(detected.data)
                detected
            }
        }
}
