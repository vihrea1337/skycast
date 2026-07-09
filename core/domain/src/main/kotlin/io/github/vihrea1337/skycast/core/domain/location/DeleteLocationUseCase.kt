package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import javax.inject.Inject

/** Удаление выбранного города переназначает выбор на первый оставшийся (в репозитории). */
class DeleteLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(id: Long) = locationRepository.delete(id)
}
