package io.github.vihrea1337.skycast.feature.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.vihrea1337.skycast.core.designsystem.component.WeatherIcon
import io.github.vihrea1337.skycast.core.model.HourlyForecast
import io.github.vihrea1337.skycast.feature.home.R
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val ItemWidth = 56.dp
private val ItemSpacing = 4.dp
private val CurveHeight = 40.dp

/**
 * Прогноз по часам: колонки (время, значок, температура, осадки) и под ними —
 * кривая температуры, нарисованная на Canvas.
 *
 * Обычный `Row` с прокруткой вместо `LazyRow`: часов всего 24, ленивость не
 * нужна, зато кривая — один Canvas во всю ширину ряда и прокручивается вместе
 * с колонками, а её точки стоят ровно под центрами колонок.
 */
@Composable
internal fun HourlyForecastRow(
    hours: List<HourlyForecast>,
    modifier: Modifier = Modifier,
) {
    Column(modifier.horizontalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(ItemSpacing)) {
            hours.forEachIndexed { index, hour ->
                HourlyItem(hour = hour, isNow = index == 0)
            }
        }
        if (hours.size >= 2) {
            TemperatureCurve(
                temperatures = hours.map { it.temperature },
                modifier = Modifier
                    .width(ItemWidth * hours.size + ItemSpacing * (hours.size - 1))
                    .height(CurveHeight),
            )
        }
    }
}

/**
 * Плавная кривая (кубические Безье между соседними точками) с заливкой-градиентом
 * под ней и точкой на каждом часе. Высоты точек считает чистая функция
 * [normalizeTemperatures] — она же и тестируется.
 */
@Composable
private fun TemperatureCurve(temperatures: List<Double>, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary
    val description = stringResource(
        R.string.feature_home_temperature_curve,
        temperatures.min().roundToInt(),
        temperatures.max().roundToInt(),
    )
    Canvas(
        modifier
            .testTag("temperature_curve")
            .semantics { contentDescription = description },
    ) {
        val levels = normalizeTemperatures(temperatures)
        val step = (ItemWidth + ItemSpacing).toPx()
        val pad = 6.dp.toPx()
        val usable = size.height - pad * 2
        val points = levels.mapIndexed { i, level ->
            // level 1.0 — самый тёплый час, рисуется сверху
            Offset(ItemWidth.toPx() / 2 + i * step, pad + usable * (1f - level))
        }
        val curve = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val cur = points[i]
                val midX = (prev.x + cur.x) / 2
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }
        val fill = Path().apply {
            addPath(curve)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(line.copy(alpha = 0.25f), line.copy(alpha = 0f))))
        drawPath(curve, line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        points.forEach { drawCircle(line, radius = 3.dp.toPx(), center = it) }
    }
}

/**
 * Температуры → высоты точек от 0 (самый холодный час) до 1 (самый тёплый).
 * Если весь день одна температура — ровная линия посередине, а не деление на ноль.
 */
internal fun normalizeTemperatures(temperatures: List<Double>): List<Float> {
    if (temperatures.isEmpty()) return emptyList()
    val min = temperatures.min()
    val range = temperatures.max() - min
    return if (range == 0.0) {
        temperatures.map { FLAT_LEVEL }
    } else {
        temperatures.map { ((it - min) / range).toFloat() }
    }
}

private const val FLAT_LEVEL = 0.5f

@Composable
private fun HourlyItem(
    hour: HourlyForecast,
    isNow: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(ItemWidth),
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
