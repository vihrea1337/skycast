package io.github.vihrea1337.skycast.feature.home

import app.cash.turbine.test
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.domain.location.ObserveSelectedLocationUseCase
import io.github.vihrea1337.skycast.core.domain.weather.ObserveWeatherUseCase
import io.github.vihrea1337.skycast.core.domain.weather.RefreshWeatherUseCase
import io.github.vihrea1337.skycast.core.testing.FakeLocationRepository
import io.github.vihrea1337.skycast.core.testing.FakeWeatherRepository
import io.github.vihrea1337.skycast.core.testing.MainDispatcherRule
import io.github.vihrea1337.skycast.core.testing.sampleLocation
import io.github.vihrea1337.skycast.core.testing.sampleWeather
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now: Instant = Instant.parse("2026-07-09T12:00:00Z")
    private val fixedClock: Clock = Clock.fixed(now, ZoneOffset.UTC)

    private val weatherRepository = FakeWeatherRepository()
    private val locationRepository = FakeLocationRepository()

    private fun viewModel() = HomeViewModel(
        observeWeather = ObserveWeatherUseCase(weatherRepository),
        observeSelectedLocation = ObserveSelectedLocationUseCase(locationRepository),
        refreshWeather = RefreshWeatherUseCase(weatherRepository),
        clock = fixedClock,
    )

    @Test
    fun `no selected city shows NoCity and never refreshes`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.NoCity, awaitNonLoading())
        }
        assertEquals(0, weatherRepository.refreshCount)
    }

    @Test
    fun `fresh cache shows content without extra refresh`() = runTest {
        locationRepository.saveAndSelect(sampleLocation())
        weatherRepository.weather.value = sampleWeather(updatedAt = now)
        val viewModel = viewModel()

        viewModel.uiState.test {
            val content = awaitNonLoading() as HomeUiState.Content
            assertEquals(21.4, content.weather.current.temperature, 0.0)
            assertNull(content.transientError)
        }
        assertEquals(0, weatherRepository.refreshCount)
    }

    @Test
    fun `stale cache triggers refresh on start`() = runTest {
        locationRepository.saveAndSelect(sampleLocation())
        weatherRepository.weather.value =
            sampleWeather(updatedAt = now.minus(Duration.ofMinutes(31)))

        viewModel()

        assertEquals(1, weatherRepository.refreshCount)
    }

    @Test
    fun `refresh failure without cache shows full screen error`() = runTest {
        locationRepository.saveAndSelect(sampleLocation())
        weatherRepository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Error(AppError.Network), awaitNonLoading())
        }
    }

    @Test
    fun `refresh failure with cache keeps content and exposes transient error`() = runTest {
        locationRepository.saveAndSelect(sampleLocation())
        weatherRepository.weather.value =
            sampleWeather(updatedAt = now.minus(Duration.ofMinutes(31))) // протухло → init дёрнет refresh
        weatherRepository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()

        viewModel.uiState.test {
            val content = awaitNonLoading() as HomeUiState.Content
            assertEquals(AppError.Network, content.transientError)

            viewModel.onTransientErrorShown()
            val cleared = awaitItem() as HomeUiState.Content
            assertNull(cleared.transientError)
        }
    }

    @Test
    fun `selecting another city refreshes when its cache is empty`() = runTest {
        locationRepository.saveAndSelect(sampleLocation(id = 1, name = "Петрозаводск"))
        viewModel()
        assertEquals(1, weatherRepository.refreshCount) // кэша нет — init обновил

        locationRepository.saveAndSelect(sampleLocation(id = 2, name = "Москва"))

        assertEquals(2, weatherRepository.refreshCount)
    }

    /** StateFlow стартует с Loading — пропускаем его, если успели увидеть. */
    private suspend fun app.cash.turbine.TurbineTestContext<HomeUiState>.awaitNonLoading(): HomeUiState {
        var state = awaitItem()
        while (state == HomeUiState.Loading) {
            state = awaitItem()
        }
        return state
    }
}
