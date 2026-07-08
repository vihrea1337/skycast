package io.github.vihrea1337.skycast.core.network.retrofit

import io.github.vihrea1337.skycast.core.network.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface GeocodingApi {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("language") language: String,
        @Query("count") count: Int = DEFAULT_COUNT,
    ): GeocodingResponseDto

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
        const val DEFAULT_COUNT = 5
    }
}
