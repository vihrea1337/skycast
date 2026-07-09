package io.github.vihrea1337.skycast.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.vihrea1337.skycast.core.designsystem.R
import io.github.vihrea1337.skycast.core.model.WeatherType

/**
 * Единая точка «тип погоды → иконка»: собственный векторный набор,
 * один и тот же в приложении и в виджете.
 */
@Composable
fun WeatherIcon(
    type: WeatherType,
    isDay: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(type.drawableRes(isDay)),
        contentDescription = stringResource(type.labelRes()),
        modifier = modifier,
    )
}

/** Публичный маппинг: виджет использует его же через ImageProvider. */
@DrawableRes
fun WeatherType.drawableRes(isDay: Boolean): Int = when (this) {
    WeatherType.CLEAR -> if (isDay) R.drawable.ic_weather_clear else R.drawable.ic_weather_night
    WeatherType.PARTLY_CLOUDY -> if (isDay) R.drawable.ic_weather_partly else R.drawable.ic_weather_clouds
    WeatherType.OVERCAST -> R.drawable.ic_weather_clouds
    WeatherType.FOG -> R.drawable.ic_weather_fog
    WeatherType.DRIZZLE, WeatherType.RAIN, WeatherType.RAIN_SHOWERS -> R.drawable.ic_weather_rain
    WeatherType.SNOW, WeatherType.SNOW_SHOWERS -> R.drawable.ic_weather_snow
    WeatherType.THUNDERSTORM -> R.drawable.ic_weather_thunder
}

@StringRes
fun WeatherType.labelRes(): Int = when (this) {
    WeatherType.CLEAR -> R.string.core_designsystem_weather_clear
    WeatherType.PARTLY_CLOUDY -> R.string.core_designsystem_weather_partly_cloudy
    WeatherType.OVERCAST -> R.string.core_designsystem_weather_overcast
    WeatherType.FOG -> R.string.core_designsystem_weather_fog
    WeatherType.DRIZZLE -> R.string.core_designsystem_weather_drizzle
    WeatherType.RAIN -> R.string.core_designsystem_weather_rain
    WeatherType.RAIN_SHOWERS -> R.string.core_designsystem_weather_rain_showers
    WeatherType.SNOW -> R.string.core_designsystem_weather_snow
    WeatherType.SNOW_SHOWERS -> R.string.core_designsystem_weather_snow_showers
    WeatherType.THUNDERSTORM -> R.string.core_designsystem_weather_thunderstorm
}
