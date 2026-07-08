package io.github.vihrea1337.skycast.core.network.retrofit

import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ForecastApi {

    // Retrofit-метод: каждый параметр = query-параметр запроса, в data class их не собрать
    @Suppress("LongParameterList")
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("temperature_unit") temperatureUnit: String,
        @Query("wind_speed_unit") windSpeedUnit: String,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
    ): ForecastResponseDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"

        const val FORECAST_DAYS = 7
        const val CURRENT_FIELDS = "temperature_2m,apparent_temperature,relative_humidity_2m," +
            "weather_code,wind_speed_10m,surface_pressure,is_day"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset"
    }
}
