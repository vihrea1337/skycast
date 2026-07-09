package io.github.vihrea1337.skycast.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.vihrea1337.skycast.core.designsystem.component.WeatherIcon
import io.github.vihrea1337.skycast.core.model.HourlyForecast
import io.github.vihrea1337.skycast.feature.home.R
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
internal fun HourlyForecastRow(
    hours: List<HourlyForecast>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(hours, key = { _, hour -> hour.time.toString() }) { index, hour ->
            HourlyItem(hour = hour, isNow = index == 0)
        }
    }
}

@Composable
private fun HourlyItem(
    hour: HourlyForecast,
    isNow: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (isNow) {
                stringResource(R.string.feature_home_hour_now)
            } else {
                hour.time.format(hourFormatter)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WeatherIcon(
            type = hour.weatherType,
            isDay = hour.isDay,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = "${hour.temperature.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            // Держим место под процент осадков, чтобы карточки не прыгали по высоте
            text = if (hour.precipitationProbability > 0) "${hour.precipitationProbability}%" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
