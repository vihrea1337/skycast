package io.github.vihrea1337.skycast.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme
import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.DailyForecast
import io.github.vihrea1337.skycast.core.model.HourlyForecast
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import io.github.vihrea1337.skycast.core.designsystem.R as DesignR

/**
 * UI-тесты главного экрана (Compose, на эмуляторе):
 * `./gradlew :feature:home:connectedDebugAndroidTest`.
 *
 * Экран без состояния — `HomeScreen(state, callbacks)`, поэтому ему просто подаётся
 * нужный `HomeUiState`, без Hilt, сети и базы. Тексты берутся из ресурсов, а не
 * вписываются строками, — тест не зависит от языка эмулятора.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setScreen(
        state: HomeUiState,
        onRefresh: () -> Unit = {},
        onSearchClick: () -> Unit = {},
    ) {
        compose.setContent {
            SkyCastTheme(dynamicColor = false) {
                HomeScreen(
                    state = state,
                    onRefresh = onRefresh,
                    onTransientErrorShown = {},
                    onSearchClick = onSearchClick,
                    onSettingsClick = {},
                )
            }
        }
    }

    @Test
    fun noCity_showsHintAndOpensSearch() {
        var searchClicks = 0
        setScreen(HomeUiState.NoCity, onSearchClick = { searchClicks++ })

        compose.onNodeWithText(context.getString(R.string.feature_home_no_city_message)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.feature_home_no_city_action)).performClick()
        assertEquals(1, searchClicks)
    }

    @Test
    fun networkErrorWithoutCache_showsMessageAndRetry() {
        var refreshes = 0
        setScreen(HomeUiState.Error(AppError.Network), onRefresh = { refreshes++ })

        compose.onNodeWithText(context.getString(R.string.feature_home_error_network)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(DesignR.string.core_designsystem_retry)).performClick()
        assertEquals(1, refreshes)
    }

    @Test
    fun content_showsCityTemperatureHourlyAndCurve() {
        setScreen(HomeUiState.Content(sampleWeather, isRefreshing = false, transientError = null))

        compose.onNodeWithText("Petrozavodsk").assertIsDisplayed()
        compose.onNodeWithText("21°").assertIsDisplayed()
        // Прогноз — ниже первого экрана (зависит от размера экрана эмулятора), поэтому проверяем,
        // что он есть в дереве, а не что виден без прокрутки.
        compose.onNodeWithText(context.getString(R.string.feature_home_hour_now)).assertExists()
        // Кривая температуры нарисована под часами (Canvas с testTag).
        compose.onNodeWithTag("temperature_curve").assertExists()
    }

    private val sampleWeather = WeatherData(
        location = Location(1L, "Petrozavodsk", "Karelia", "Russia", 61.78, 34.35),
        current = CurrentWeather(
            temperature = 21.4,
            feelsLike = 19.8,
            humidityPercent = 62,
            pressureHpa = 1006.9,
            windSpeed = 4.2,
            weatherType = WeatherType.CLEAR,
            isDay = true,
        ),
        hourly = (0 until 6).map { offset ->
            HourlyForecast(
                time = LocalDateTime.of(2026, 7, 9, 14 + offset, 0),
                // Разные температуры, чтобы «21°» сверху был единственным совпадением
                temperature = 15.0 + offset,
                weatherType = WeatherType.PARTLY_CLOUDY,
                precipitationProbability = 0,
                isDay = true,
            )
        },
        daily = listOf(
            DailyForecast(
                date = LocalDate.of(2026, 7, 9),
                tempMin = 12.0,
                tempMax = 23.0,
                weatherType = WeatherType.CLEAR,
                sunrise = LocalDateTime.of(2026, 7, 9, 3, 57),
                sunset = LocalDateTime.of(2026, 7, 9, 22, 12),
            ),
        ),
        updatedAt = Instant.now(),
        temperatureUnit = TemperatureUnit.CELSIUS,
        windSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
    )
}
