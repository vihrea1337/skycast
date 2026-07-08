package io.github.vihrea1337.skycast.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherTypeTest {

    @Test
    fun `maps every known WMO code group`() {
        val expectations = mapOf(
            0 to WeatherType.CLEAR,
            1 to WeatherType.PARTLY_CLOUDY,
            2 to WeatherType.PARTLY_CLOUDY,
            3 to WeatherType.OVERCAST,
            45 to WeatherType.FOG,
            48 to WeatherType.FOG,
            51 to WeatherType.DRIZZLE,
            55 to WeatherType.DRIZZLE,
            57 to WeatherType.DRIZZLE,
            61 to WeatherType.RAIN,
            65 to WeatherType.RAIN,
            67 to WeatherType.RAIN,
            71 to WeatherType.SNOW,
            75 to WeatherType.SNOW,
            77 to WeatherType.SNOW,
            80 to WeatherType.RAIN_SHOWERS,
            82 to WeatherType.RAIN_SHOWERS,
            85 to WeatherType.SNOW_SHOWERS,
            86 to WeatherType.SNOW_SHOWERS,
            95 to WeatherType.THUNDERSTORM,
            96 to WeatherType.THUNDERSTORM,
            99 to WeatherType.THUNDERSTORM,
        )

        expectations.forEach { (code, expected) ->
            assertEquals("WMO code $code", expected, WeatherType.fromWmoCode(code))
        }
    }

    @Test
    fun `unknown code degrades to overcast instead of crashing`() {
        assertEquals(WeatherType.OVERCAST, WeatherType.fromWmoCode(42))
        assertEquals(WeatherType.OVERCAST, WeatherType.fromWmoCode(-1))
        assertEquals(WeatherType.OVERCAST, WeatherType.fromWmoCode(100))
    }
}
