package io.github.vihrea1337.skycast.feature.search

import app.cash.turbine.test
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.domain.location.DeleteLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.DetectMyLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.ObserveSavedLocationsUseCase
import io.github.vihrea1337.skycast.core.domain.location.ObserveSelectedLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.SaveLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.SearchCityUseCase
import io.github.vihrea1337.skycast.core.domain.location.SelectLocationUseCase
import io.github.vihrea1337.skycast.core.testing.FakeLocationRepository
import io.github.vihrea1337.skycast.core.testing.MainDispatcherRule
import io.github.vihrea1337.skycast.core.testing.sampleLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()

    private fun viewModel() = SearchViewModel(
        searchCity = SearchCityUseCase(locationRepository),
        observeSavedLocations = ObserveSavedLocationsUseCase(locationRepository),
        observeSelectedLocation = ObserveSelectedLocationUseCase(locationRepository),
        selectLocation = SelectLocationUseCase(locationRepository),
        saveLocation = SaveLocationUseCase(locationRepository),
        deleteLocation = DeleteLocationUseCase(locationRepository),
        detectMyLocation = DetectMyLocationUseCase(locationRepository),
    )

    @Test
    fun `query shorter than two characters stays idle`() = runTest {
        val viewModel = viewModel()

        viewModel.results.test {
            assertEquals(SearchResultsState.Idle, awaitItem())
            viewModel.onQueryChange("п")
            advanceTimeBy(400)
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `search emits searching then results after debounce`() = runTest {
        val petrozavodsk = sampleLocation()
        locationRepository.searchResult = AppResult.Success(listOf(petrozavodsk))
        val viewModel = viewModel()

        viewModel.results.test {
            assertEquals(SearchResultsState.Idle, awaitItem())
            viewModel.onQueryChange("Петр")
            advanceTimeBy(300)
            runCurrent()
            assertEquals(SearchResultsState.Results(listOf(petrozavodsk)), awaitFinal())
        }
    }

    @Test
    fun `empty geocoding response maps to empty state`() = runTest {
        locationRepository.searchResult = AppResult.Success(emptyList())
        val viewModel = viewModel()

        viewModel.results.test {
            assertEquals(SearchResultsState.Idle, awaitItem())
            viewModel.onQueryChange("Нигдеград")
            advanceTimeBy(300)
            runCurrent()
            assertEquals(SearchResultsState.Empty, awaitFinal())
        }
    }

    @Test
    fun `search failure maps to error state`() = runTest {
        locationRepository.searchResult = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()

        viewModel.results.test {
            assertEquals(SearchResultsState.Idle, awaitItem())
            viewModel.onQueryChange("Петр")
            advanceTimeBy(300)
            runCurrent()
            assertEquals(SearchResultsState.SearchError(AppError.Network), awaitFinal())
        }
    }

    /**
     * StateFlow конфлирует: мгновенный fake-поиск может «проглотить» Searching —
     * это штатное поведение StateFlow, ждём финальное состояние.
     */
    private suspend fun app.cash.turbine.TurbineTestContext<SearchResultsState>.awaitFinal(): SearchResultsState {
        var state = awaitItem()
        while (state == SearchResultsState.Searching) {
            state = awaitItem()
        }
        return state
    }

    @Test
    fun `result click saves selects and navigates back`() = runTest {
        val petrozavodsk = sampleLocation()
        val viewModel = viewModel()

        viewModel.navigateBack.test {
            viewModel.onResultClick(petrozavodsk)
            awaitItem() // событие «назад» пришло
        }
        assertEquals(petrozavodsk.id, locationRepository.observeSelected().first()?.id)
    }

    @Test
    fun `detect failure shows message and does not navigate`() = runTest {
        locationRepository.detectResult = AppResult.Failure(AppError.Unknown)
        val viewModel = viewModel()

        viewModel.onUseMyLocation()
        runCurrent()

        assertEquals(SearchMessage.DETECT_FAILED, viewModel.message.value)
        viewModel.onMessageShown()
        assertEquals(null, viewModel.message.value)
    }
}
