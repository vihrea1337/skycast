package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.model.Location
import javax.inject.Inject

class SearchCityUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(query: String): AppResult<List<Location>> =
        locationRepository.search(query.trim())
}
