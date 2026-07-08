package io.github.vihrea1337.skycast.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dehaze
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import io.github.vihrea1337.skycast.core.designsystem.R
import io.github.vihrea1337.skycast.core.model.WeatherType

/**
 * Единая точка «тип погоды → иконка». Пока это Material-иконки;
 * на этапе полировки заменятся на полноценный погодный набор — только здесь.
 */
@Composable
fun WeatherIcon(
    type: WeatherType,
    isDay: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = type.icon(isDay),
        contentDescription = stringResource(type.labelRes()),
        modifier = modifier,
    )
}

private fun WeatherType.icon(isDay: Boolean): ImageVector = when (this) {
    WeatherType.CLEAR -> if (isDay) Icons.Outlined.WbSunny else Icons.Outlined.NightsStay
    WeatherType.PARTLY_CLOUDY -> Icons.Outlined.WbCloudy
    WeatherType.OVERCAST -> Icons.Outlined.Cloud
    WeatherType.FOG -> Icons.Outlined.Dehaze
    WeatherType.DRIZZLE -> Icons.Outlined.Grain
    WeatherType.RAIN, WeatherType.RAIN_SHOWERS -> Icons.Outlined.WaterDrop
    WeatherType.SNOW, WeatherType.SNOW_SHOWERS -> Icons.Outlined.AcUnit
    WeatherType.THUNDERSTORM -> Icons.Outlined.Bolt
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
