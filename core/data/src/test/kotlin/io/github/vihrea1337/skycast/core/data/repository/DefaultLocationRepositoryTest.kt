package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.location.CurrentPositionProvider
import io.github.vihrea1337.skycast.core.data.testdouble.FakeLocationDao
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.PlaceDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultLocationRepositoryTest {

    private val locationDao = FakeLocationDao()

    @Test
    fun `search maps geocoding results to domain locations`() = runTest {
        val repository = repository(
            FakeNetworkDataSource(
                places = listOf(
                    PlaceDto(
                        id = 509820,
                        name = "Петрозаводск",
                        latitude = 61.78,
                        longitude = 34.35,
                        country = "Россия",
                        region = "Карелия",
                    ),
                ),
            ),
        )

        val result = repository.search("Петрозаводск")

        val locations = (result as AppResult.Success).data
        assertEquals(1, locations.size)
        assertEquals("Карелия", locations.first().region)
    }

    @Test
    fun `saveAndSelect makes location selected`() = runTest {
        val repository = repository(FakeNetworkDataSource())

        repository.saveAndSelect(petrozavodsk)

        assertEquals(petrozavodsk.id, repository.observeSelected().first()?.id)
        assertEquals(listOf(petrozavodsk.id), repository.observeSavedLocations().first().map { it.id })
    }

    @Test
    fun `changing selection refreshes the widget even without a network sync`() = runTest {
        // Регрессия: виджет застревал на старом городе, если у нового кэш был свежим
        val repository = repository(FakeNetworkDataSource())
        repository.saveAndSelect(petrozavodsk)
        repository.saveAndSelect(moscow)
        widgetUpdater.updateCount = 0

        repository.select(petrozavodsk.id)

        assertEquals(1, widgetUpdater.updateCount)
    }

    @Test
    fun `deleting selected location reselects first remaining`() = runTest {
        val repository = repository(FakeNetworkDataSource())
        repository.saveAndSelect(petrozavodsk)
        repository.saveAndSelect(moscow)

        repository.delete(moscow.id)

        assertEquals(petrozavodsk.id, repository.observeSelected().first()?.id)
    }

    @Test
    fun `detectCurrent picks the nearest geocoding match`() = runTest {
        val repository = repository(
            network = FakeNetworkDataSource(
                places = listOf(
                    PlaceDto(id = 1, name = "Петрозаводск", latitude = 61.78, longitude = 34.35),
                    PlaceDto(id = 2, name = "Петрозаводское", latitude = 49.0, longitude = 36.0),
                ),
            ),
            positionProvider = FakePositionProvider(
                position = CurrentPositionProvider.Position(latitude = 61.79, longitude = 34.34),
                locality = "Петрозаводск",
            ),
        )

        val result = repository.detectCurrent()

        assertEquals(1L, (result as AppResult.Success).data.id)
    }

    @Test
    fun `detectCurrent without position fails gracefully`() = runTest {
        val repository = repository(FakeNetworkDataSource())

        val result = repository.detectCurrent()

        assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
    }

    @Test
    fun `deleting the last location leaves nothing selected`() = runTest {
        val repository = repository(FakeNetworkDataSource())
        repository.saveAndSelect(petrozavodsk)

        repository.delete(petrozavodsk.id)

        assertNull(repository.observeSelected().first())
        assertEquals(emptyList<Location>(), repository.observeSavedLocations().first())
    }

    private val widgetUpdater = RecordingWidgetUpdater()

    private fun TestScope.repository(
        network: NetworkDataSource,
        positionProvider: CurrentPositionProvider = FakePositionProvider(position = null, locality = null),
    ) = DefaultLocationRepository(
        locationDao = locationDao,
        network = network,
        positionProvider = positionProvider,
        widgetUpdater = widgetUpdater,
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private class RecordingWidgetUpdater : io.github.vihrea1337.skycast.core.data.widget.WidgetUpdater {
        var updateCount = 0
        override suspend fun updateAll() {
            updateCount++
        }
    }

    private class FakePositionProvider(
        private val position: CurrentPositionProvider.Position?,
        private val locality: String?,
    ) : CurrentPositionProvider {
        override suspend fun currentPosition(): CurrentPositionProvider.Position? = position
        override suspend fun localityName(position: CurrentPositionProvider.Position): String? = locality
    }

    private class FakeNetworkDataSource(
        private val places: List<PlaceDto> = emptyList(),
    ) : NetworkDataSource {

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            temperatureUnit: TemperatureUnit,
            windSpeedUnit: WindSpeedUnit,
        ): AppResult<ForecastResponseDto> = error("not used")

        override suspend fun searchPlaces(query: String, language: String): AppResult<List<PlaceDto>> =
            AppResult.Success(places)
    }

    private val petrozavodsk = Location(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
    )

    private val moscow = Location(
        id = 524901,
        name = "Москва",
        region = null,
        country = "Россия",
        latitude = 55.75,
        longitude = 37.62,
    )
}
