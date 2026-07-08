package io.github.vihrea1337.skycast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.designsystem.component.ErrorContent
import io.github.vihrea1337.skycast.core.designsystem.component.MetricChip
import io.github.vihrea1337.skycast.core.designsystem.component.WeatherIcon
import io.github.vihrea1337.skycast.core.designsystem.component.labelRes
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme
import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

/** Stateful-обвязка: связывает ViewModel и stateless-экран (конвенция из docs/PLAN.md §2.7). */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = uiState,
        onRefresh = viewModel::onRefresh,
        onTransientErrorShown = viewModel::onTransientErrorShown,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onTransientErrorShown: () -> Unit,
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

    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            HomeUiState.Loading -> LoadingContent()

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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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
            text = weather.location.name,
            style = MaterialTheme.typography.headlineMedium,
        )
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
                value = stringResource(R.string.feature_home_wind_value, "%.1f".format(current.windSpeed)),
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
    }
}

/** «21°», «-7°»: округление до целого — точнее для погоды не нужно. */
private fun formatTemperature(value: Double): String = "${value.roundToInt()}°"

@Composable
private fun updatedAgoText(updatedAt: Instant): String {
    val minutes = Duration.between(updatedAt, Instant.now()).toMinutes()
    return when {
        minutes < 1 -> stringResource(R.string.feature_home_updated_just_now)
        minutes < MINUTES_IN_HOUR -> stringResource(R.string.feature_home_updated_minutes_ago, minutes)
        else -> stringResource(R.string.feature_home_updated_hours_ago, minutes / MINUTES_IN_HOUR)
    }
}

private const val MINUTES_IN_HOUR = 60L

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
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    SkyCastTheme {
        HomeScreen(
            state = HomeUiState.Error(AppError.Network),
            onRefresh = {},
            onTransientErrorShown = {},
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
    hourly = emptyList(),
    daily = emptyList(),
    updatedAt = Instant.now(),
)
