package io.github.vihrea1337.skycast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.designsystem.component.ErrorContent
import io.github.vihrea1337.skycast.core.designsystem.component.MetricChip
import io.github.vihrea1337.skycast.core.designsystem.component.SectionCard
import io.github.vihrea1337.skycast.core.designsystem.component.WeatherIcon
import io.github.vihrea1337.skycast.core.designsystem.component.labelRes
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme
import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.feature.home.components.DailyForecastList
import io.github.vihrea1337.skycast.feature.home.components.HomeSkeleton
import io.github.vihrea1337.skycast.feature.home.components.HourlyForecastRow
import io.github.vihrea1337.skycast.feature.home.components.SkyBackdrop
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

/** Stateful-обвязка: связывает ViewModel и stateless-экран (конвенция из docs/PLAN.md §2.7). */
@Composable
fun HomeRoute(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = uiState,
        onRefresh = viewModel::onRefresh,
        onTransientErrorShown = viewModel::onTransientErrorShown,
        onSearchClick = onSearchClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onTransientErrorShown: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val transientMessage = (state as? HomeUiState.Content)?.transientError?.toMessage()
    LaunchedEffect(transientMessage) {
        if (transientMessage != null) {
            snackbarHostState.showSnackbar(transientMessage)
            onTransientErrorShown()
        }
    }

    val current = (state as? HomeUiState.Content)?.weather?.current
    // Фон — «небо» текущей погоды; без данных (загрузка, ошибка, нет города) — обычная поверхность.
    SkyBackdrop(type = current?.weatherType, isDay = current?.isDay ?: true, modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                // Прозрачная панель — небо видно и под заголовком с названием города.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text((state as? HomeUiState.Content)?.weather?.location?.name ?: "SkyCast")
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.feature_home_action_search),
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.feature_home_action_settings),
                        )
                    }
                },
            )
            when (state) {
                HomeUiState.Loading -> HomeSkeleton()

                HomeUiState.NoCity -> NoCityContent(onSearchClick = onSearchClick)

                is HomeUiState.Error -> ErrorContent(
                    message = state.error.toMessage(),
                    onRetry = onRefresh,
                )

                is HomeUiState.Content -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    WeatherContent(weather = state.weather)
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun NoCityContent(onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_home_no_city_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onSearchClick) {
            Text(stringResource(R.string.feature_home_no_city_action))
        }
    }
}

@Composable
private fun WeatherContent(weather: WeatherData, modifier: Modifier = Modifier) {
    val current = weather.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = updatedAgoText(weather.updatedAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        WeatherIcon(
            type = current.weatherType,
            isDay = current.isDay,
            modifier = Modifier.size(96.dp),
        )
        Text(
            text = formatTemperature(current.temperature),
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = stringResource(current.weatherType.labelRes()),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(
                R.string.feature_home_feels_like,
                formatTemperature(current.feelsLike),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.feature_home_wind),
                // Подпись единиц — из units-at-sync, а не из настроек (docs/PLAN.md §5.12)
                value = stringResource(
                    weather.windSpeedUnit.valueRes(),
                    "%.1f".format(current.windSpeed),
                ),
            )
            MetricChip(
                label = stringResource(R.string.feature_home_humidity),
                value = stringResource(R.string.feature_home_humidity_value, current.humidityPercent),
            )
            MetricChip(
                label = stringResource(R.string.feature_home_pressure),
                value = stringResource(R.string.feature_home_pressure_value, current.pressureHpa.roundToInt()),
            )
        }
        if (weather.hourly.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            SectionCard(title = stringResource(R.string.feature_home_hourly_title)) {
                HourlyForecastRow(hours = weather.hourly)
            }
        }
        if (weather.daily.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionCard(title = stringResource(R.string.feature_home_daily_title)) {
                DailyForecastList(days = weather.daily)
            }
        }
    }
}

/** «21°», «-7°»: округление до целого — точнее для погоды не нужно. */
private fun formatTemperature(value: Double): String = "${value.roundToInt()}°"

private fun WindSpeedUnit.valueRes(): Int = when (this) {
    WindSpeedUnit.METERS_PER_SECOND -> R.string.feature_home_wind_value_ms
    WindSpeedUnit.KILOMETERS_PER_HOUR -> R.string.feature_home_wind_value_kmh
}

@Composable
private fun updatedAgoText(updatedAt: Instant): String {
    // Метка «живая»: тикает раз в минуту, а не замирает до следующей рекомпозиции
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(updatedAt) {
        while (true) {
            delay(TICK_MS)
            now = Instant.now()
        }
    }
    val minutes = Duration.between(updatedAt, now).toMinutes()
    return when {
        minutes < 1 -> stringResource(R.string.feature_home_updated_just_now)
        minutes < MINUTES_IN_HOUR -> stringResource(R.string.feature_home_updated_minutes_ago, minutes)
        else -> stringResource(R.string.feature_home_updated_hours_ago, minutes / MINUTES_IN_HOUR)
    }
}

private const val MINUTES_IN_HOUR = 60L
private const val TICK_MS = 60_000L

@Composable
private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.feature_home_error_network)
    is AppError.Server -> stringResource(R.string.feature_home_error_server, code)
    AppError.Unknown -> stringResource(R.string.feature_home_error_unknown)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    SkyCastTheme {
        HomeScreen(
            state = HomeUiState.Content(previewWeather, isRefreshing = false, transientError = null),
            onRefresh = {},
            onTransientErrorShown = {},
            onSearchClick = {},
            onSettingsClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNoCityPreview() {
    SkyCastTheme {
        HomeScreen(
            state = HomeUiState.NoCity,
            onRefresh = {},
            onTransientErrorShown = {},
            onSearchClick = {},
            onSettingsClick = {},
        )
    }
}

private val previewWeather = WeatherData(
    location = Location(
        id = 1L,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
    ),
    current = CurrentWeather(
        temperature = 21.4,
        feelsLike = 19.8,
        humidityPercent = 62,
        pressureHpa = 1006.9,
        windSpeed = 4.2,
        weatherType = WeatherType.PARTLY_CLOUDY,
        isDay = true,
    ),
    hourly = (0 until 6).map { offset ->
        io.github.vihrea1337.skycast.core.model.HourlyForecast(
            time = java.time.LocalDateTime.of(2026, 7, 9, 14 + offset, 0),
            temperature = 21.0 + offset,
            weatherType = if (offset < 3) WeatherType.PARTLY_CLOUDY else WeatherType.RAIN,
            precipitationProbability = if (offset < 3) 0 else 55,
            isDay = offset < 4,
        )
    },
    daily = (0 until 3).map { offset ->
        io.github.vihrea1337.skycast.core.model.DailyForecast(
            date = java.time.LocalDate.of(2026, 7, 9).plusDays(offset.toLong()),
            tempMin = 14.0 + offset,
            tempMax = 22.0 + offset,
            weatherType = WeatherType.PARTLY_CLOUDY,
            sunrise = java.time.LocalDateTime.of(2026, 7, 9, 3, 57),
            sunset = java.time.LocalDateTime.of(2026, 7, 9, 22, 12),
        )
    },
    updatedAt = Instant.now(),
    temperatureUnit = TemperatureUnit.CELSIUS,
    windSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
)
