package io.github.vihrea1337.skycast.feature.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.vihrea1337.skycast.core.designsystem.component.WeatherIcon
import io.github.vihrea1337.skycast.core.model.DailyForecast
import io.github.vihrea1337.skycast.feature.home.R
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
internal fun DailyForecastList(
    days: List<DailyForecast>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        days.forEachIndexed { index, day ->
            DailyItem(day = day, isToday = index == 0)
        }
    }
}

@Composable
private fun DailyItem(
    day: DailyForecast,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isToday) stringResource(R.string.feature_home_today) else day.dayLabel(),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (isToday) {
                Text(
                    text = stringResource(
                        R.string.feature_home_sun_times,
                        day.sunrise.format(timeFormatter),
                        day.sunset.format(timeFormatter),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Дневной прогноз — всегda «дневная» иконка
        WeatherIcon(
            type = day.weatherType,
            isDay = true,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(
                R.string.feature_home_day_temps,
                day.tempMax.roundToInt(),
                day.tempMin.roundToInt(),
            ),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** «ср, 15 июл» по локали устройства. */
private fun DailyForecast.dayLabel(): String {
    val locale = Locale.getDefault()
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    return "$dayOfWeek, ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, locale)}"
}
