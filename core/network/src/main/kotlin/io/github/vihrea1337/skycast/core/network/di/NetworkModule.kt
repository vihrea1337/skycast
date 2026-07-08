package io.github.vihrea1337.skycast.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.network.BuildConfig
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import io.github.vihrea1337.skycast.core.network.RetrofitNetworkDataSource
import io.github.vihrea1337.skycast.core.network.retrofit.ForecastApi
import io.github.vihrea1337.skycast.core.network.retrofit.GeocodingApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
internal annotation class ForecastRetrofit

@Qualifier
internal annotation class GeocodingRetrofit

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    private const val TIMEOUT_SECONDS = 10L

    @Provides
    @Singleton
    fun providesJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }
        .build()

    @Provides
    @Singleton
    @ForecastRetrofit
    fun providesForecastRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(ForecastApi.BASE_URL, client, json)

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun providesGeocodingRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(GeocodingApi.BASE_URL, client, json)

    @Provides
    fun providesForecastApi(@ForecastRetrofit retrofit: Retrofit): ForecastApi =
        retrofit.create(ForecastApi::class.java)

    @Provides
    fun providesGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)

    // Два baseUrl (forecast и geocoding) → два инстанса Retrofit на общем OkHttpClient
    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkDataSourceModule {

    @Binds
    abstract fun bindsNetworkDataSource(impl: RetrofitNetworkDataSource): NetworkDataSource
}
