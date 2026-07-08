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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import java.time.Instant
import kotlin.math.roundToInt

/** Stateful-обвязка: связывает ViewModel и stateless-экран (конвенция из docs/PLAN.md §2.7). */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = uiState, onRetry = viewModel::onRetry, modifier = modifier)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        HomeUiState.Loading -> LoadingContent(modifier)
        is HomeUiState.Error -> ErrorContent(
            message = state.error.toMessage(),
            onRetry = onRetry,
            modifier = modifier,
        )
        is HomeUiState.Content -> WeatherContent(weather = state.weather, modifier = modifier)
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
private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.feature_home_error_network)
    is AppError.Server -> stringResource(R.string.feature_home_error_server, code)
    AppError.Unknown -> stringResource(R.string.feature_home_error_unknown)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    SkyCastTheme {
        HomeScreen(state = HomeUiState.Content(previewWeather), onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    SkyCastTheme {
        HomeScreen(state = HomeUiState.Error(AppError.Network), onRetry = {})
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
    updatedAt = Instant.EPOCH,
)
