package io.github.vihrea1337.skycast.feature.home.components

import io.github.vihrea1337.skycast.core.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Чистые функции визуала главного экрана: цвет неба и высоты точек кривой температуры. */
class SkyVisualsTest {

    @Test
    fun `every weather type maps to a sky`() {
        // Новый WeatherType без «неба» не скомпилируется (when без else), но и в тесте проверим явно.
        WeatherType.entries.forEach { type ->
            listOf(true, false).forEach { isDay ->
                listOf(true, false).forEach { dark -> skyTint(type, isDay, dark) }
            }
        }
    }

    @Test
    fun `drizzle and showers share the rain sky`() {
        assertEquals(SkyGroup.RAIN, SkyGroup.of(WeatherType.DRIZZLE))
        assertEquals(SkyGroup.RAIN, SkyGroup.of(WeatherType.RAIN_SHOWERS))
        assertEquals(SkyGroup.SNOW, SkyGroup.of(WeatherType.SNOW_SHOWERS))
    }

    @Test
    fun `night and dark theme change the tint`() {
        val day = skyTint(WeatherType.CLEAR, isDay = true, darkTheme = false)
        assertNotEquals(day, skyTint(WeatherType.CLEAR, isDay = false, darkTheme = false))
        assertNotEquals(day, skyTint(WeatherType.CLEAR, isDay = true, darkTheme = true))
    }

    @Test
    fun `light theme skies are light and dark theme skies are dark`() {
        // Текст рисуется цветом темы — фон обязан ему соответствовать, иначе не прочитать.
        SkyGroup.entries.forEach { g ->
            assertTrue("$g dayLight", luminance(g.dayLight) > HALF)
            assertTrue("$g nightLight", luminance(g.nightLight) > HALF)
            assertTrue("$g dayDark", luminance(g.dayDark) < HALF)
            assertTrue("$g nightDark", luminance(g.nightDark) < HALF)
        }
    }

    @Test
    fun `temperatures are normalized from coldest 0 to warmest 1`() {
        assertEquals(listOf(0f, 0.5f, 1f, 0.25f), normalizeTemperatures(listOf(10.0, 12.0, 14.0, 11.0)))
    }

    @Test
    fun `flat day is a straight line in the middle and empty input is empty`() {
        assertEquals(listOf(0.5f, 0.5f), normalizeTemperatures(listOf(-3.0, -3.0)))
        assertEquals(emptyList<Float>(), normalizeTemperatures(emptyList()))
    }

    /** Простая яркость ARGB-цвета (0..1) — без android.graphics, чтобы тест шёл на JVM. */
    private fun luminance(argb: Long): Double {
        val r = (argb shr 16 and 0xFF) / 255.0
        val g = (argb shr 8 and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private companion object {
        const val HALF = 0.5
    }
}
