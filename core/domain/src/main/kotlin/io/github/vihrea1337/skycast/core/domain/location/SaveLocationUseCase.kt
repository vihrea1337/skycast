package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.model.Location
import javax.inject.Inject

/** Сохранить и сразу выбрать — единый сценарий клика по результату поиска. */
class SaveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(location: Location) = locationRepository.saveAndSelect(location)
}
