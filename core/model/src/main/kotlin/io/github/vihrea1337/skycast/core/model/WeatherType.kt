package io.github.vihrea1337.skycast.core.model

/** Погодные условия, сгруппированные из WMO-кодов Open-Meteo. Иконки и подписи — в designsystem. */
enum class WeatherType {
    CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    ;

    companion object {
        /**
         * Маппинг [WMO weather interpretation codes](https://open-meteo.com/en/docs),
         * которые Open-Meteo отдаёт в поле weather_code.
         */
        @Suppress("MagicNumber") // WMO-коды и есть доменные константы
        fun fromWmoCode(code: Int): WeatherType = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            in 51..57 -> DRIZZLE
            in 61..67 -> RAIN
            in 71..77 -> SNOW
            in 80..82 -> RAIN_SHOWERS
            85, 86 -> SNOW_SHOWERS
            in 95..99 -> THUNDERSTORM
            // Неизвестный код — мягкая деградация вместо крэша
            else -> OVERCAST
        }
    }
}
