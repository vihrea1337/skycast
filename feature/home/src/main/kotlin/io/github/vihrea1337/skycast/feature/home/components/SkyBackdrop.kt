package io.github.vihrea1337.skycast.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.vihrea1337.skycast.core.model.WeatherType

/**
 * Цвет неба для фона главного экрана: «погода + день/ночь + тема» → оттенок.
 *
 * Чистая функция без Compose (цвет — ARGB в Long), поэтому покрыта обычным
 * JVM-тестом. Светлая тема — пастельные оттенки (тёмный текст на них читается),
 * тёмная — глубокие (читается светлый). Ночь в любой теме уходит в индиго.
 */
internal fun skyTint(type: WeatherType, isDay: Boolean, darkTheme: Boolean): Long {
    val group = SkyGroup.of(type)
    return when {
        !isDay && darkTheme -> group.nightDark
        !isDay -> group.nightLight
        darkTheme -> group.dayDark
        else -> group.dayLight
    }
}

/** Несколько «небес» на десять типов погоды: морось и ливень выглядят одинаково. */
@Suppress("MagicNumber") // цвета — дизайн-константы
internal enum class SkyGroup(val dayLight: Long, val dayDark: Long, val nightLight: Long, val nightDark: Long) {
    CLEAR(0xFFBFE3FF, 0xFF0F3B66, 0xFFC9CCF2, 0xFF141A3D),
    CLOUDY(0xFFD3DEEA, 0xFF2A3644, 0xFFCDD0E2, 0xFF1C2130),
    FOG(0xFFE0E2E5, 0xFF33363B, 0xFFD6D7DE, 0xFF23252B),
    RAIN(0xFFC2CEDC, 0xFF1E2B3A, 0xFFBEC4D8, 0xFF161C2A),
    SNOW(0xFFE3F0FA, 0xFF2B3A48, 0xFFDCE2F2, 0xFF1F2636),
    STORM(0xFFCCC4E0, 0xFF2A1F45, 0xFFC3BCDC, 0xFF1D1633),
    ;

    companion object {
        fun of(type: WeatherType): SkyGroup = when (type) {
            WeatherType.CLEAR -> CLEAR
            WeatherType.PARTLY_CLOUDY, WeatherType.OVERCAST -> CLOUDY
            WeatherType.FOG -> FOG
            WeatherType.DRIZZLE, WeatherType.RAIN, WeatherType.RAIN_SHOWERS -> RAIN
            WeatherType.SNOW, WeatherType.SNOW_SHOWERS -> SNOW
            WeatherType.THUNDERSTORM -> STORM
        }
    }
}

/**
 * Фон «небо»: оттенок сверху плавно растворяется в цвете поверхности к середине
 * экрана, чтобы карточки ниже оставались на привычном фоне. Смена погоды или
 * города перетекает за полсекунды (`animateColorAsState`), без резкой вспышки.
 */
@Composable
internal fun SkyBackdrop(
    type: WeatherType?,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    // Тема по факту цвета поверхности: учитывает и системную, и выбранную в настройках.
    val darkTheme = surface.luminance() < DARK_LUMINANCE
    val target = if (type == null) surface else Color(skyTint(type, isDay, darkTheme))
    val top by animateColorAsState(target, tween(durationMillis = 600), label = "sky")
    Box(
        modifier = modifier.background(Brush.verticalGradient(0f to top, 0.55f to surface)),
        content = content,
    )
}

private const val DARK_LUMINANCE = 0.5f
