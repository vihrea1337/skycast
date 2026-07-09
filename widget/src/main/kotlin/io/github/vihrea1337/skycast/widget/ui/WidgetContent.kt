package io.github.vihrea1337.skycast.widget.ui

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.vihrea1337.skycast.core.designsystem.component.labelRes
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.widget.R
import io.github.vihrea1337.skycast.widget.WeatherWidget
import kotlin.math.roundToInt

// FQCN строкой: прямая зависимость :widget → :app создала бы цикл (app подключает widget)
private const val MAIN_ACTIVITY = "io.github.vihrea1337.skycast.MainActivity"

@Composable
internal fun WidgetContent(weather: WeatherData?) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(ComponentName(context.packageName, MAIN_ACTIVITY)))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            weather == null -> EmptyContent()
            LocalSize.current.width >= WeatherWidget.MEDIUM.width -> MediumContent(weather)
            else -> SmallContent(weather)
        }
    }
}

@Composable
private fun EmptyContent() {
    Text(
        text = LocalContext.current.getString(R.string.widget_no_data),
        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 13.sp),
    )
}

@Composable
private fun SmallContent(weather: WeatherData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WeatherImage(type = weather.current.weatherType, isDay = weather.current.isDay, size = 32.dp)
        Spacer(GlanceModifier.width(8.dp))
        Column {
            Text(
                text = "${weather.current.temperature.roundToInt()}°",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = weather.location.name,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MediumContent(weather: WeatherData) {
    val context = LocalContext.current
    Column {
        Text(
            text = weather.location.name,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            WeatherImage(type = weather.current.weatherType, isDay = weather.current.isDay, size = 44.dp)
            Spacer(GlanceModifier.width(12.dp))
            Text(
                text = "${weather.current.temperature.roundToInt()}°",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = context.getString(weather.current.weatherType.labelRes()),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                    maxLines = 1,
                )
                weather.daily.firstOrNull()?.let { today ->
                    Text(
                        text = "${today.tempMax.roundToInt()}° / ${today.tempMin.roundToInt()}°",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherImage(type: WeatherType, isDay: Boolean, size: androidx.compose.ui.unit.Dp) {
    Image(
        provider = ImageProvider(type.drawableRes(isDay)),
        contentDescription = null, // смысл передаёт текст рядом
        modifier = GlanceModifier.size(size),
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
    )
}

private fun WeatherType.drawableRes(isDay: Boolean): Int = when (this) {
    WeatherType.CLEAR -> if (isDay) R.drawable.ic_weather_clear else R.drawable.ic_weather_night
    WeatherType.PARTLY_CLOUDY, WeatherType.OVERCAST -> R.drawable.ic_weather_clouds
    WeatherType.FOG -> R.drawable.ic_weather_fog
    WeatherType.DRIZZLE, WeatherType.RAIN, WeatherType.RAIN_SHOWERS -> R.drawable.ic_weather_rain
    WeatherType.SNOW, WeatherType.SNOW_SHOWERS -> R.drawable.ic_weather_snow
    WeatherType.THUNDERSTORM -> R.drawable.ic_weather_thunder
}
