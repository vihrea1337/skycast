package io.github.vihrea1337.skycast.core.domain.location

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
import io.github.vihrea1337.skycast.core.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectMyLocationUseCaseTest {

    @Test
    fun `detected city is saved and selected`() = runTest {
        val repository = FakeLocationRepository(detectResult = AppResult.Success(petrozavodsk))
        val useCase = DetectMyLocationUseCase(repository)

        val result = useCase()

        assertEquals(petrozavodsk, (result as AppResult.Success).data)
        assertEquals(listOf(petrozavodsk), repository.saved)
    }

    @Test
    fun `detection failure saves nothing and passes error through`() = runTest {
        val repository = FakeLocationRepository(detectResult = AppResult.Failure(AppError.Unknown))
        val useCase = DetectMyLocationUseCase(repository)

        val result = useCase()

        assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
        assertTrue(repository.saved.isEmpty())
    }

    private class FakeLocationRepository(
        private val detectResult: AppResult<Location>,
    ) : LocationRepository {

        val saved = mutableListOf<Location>()
        private val locations = MutableStateFlow<List<Location>>(emptyList())

        override fun observeSavedLocations(): Flow<List<Location>> = locations

        override fun observeSelected(): Flow<Location?> = locations.map { it.firstOrNull() }

        override suspend fun search(query: String): AppResult<List<Location>> =
            AppResult.Success(emptyList())

        override suspend fun saveAndSelect(location: Location) {
            saved += location
        }

        override suspend fun select(id: Long) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun detectCurrent(): AppResult<Location> = detectResult
    }

    private val petrozavodsk = Location(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
    )
}
