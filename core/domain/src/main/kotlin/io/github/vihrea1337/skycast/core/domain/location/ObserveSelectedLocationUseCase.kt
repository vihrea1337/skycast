package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.model.Location
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSelectedLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    operator fun invoke(): Flow<Location?> = locationRepository.observeSelected()
}
