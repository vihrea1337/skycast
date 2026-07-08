package io.github.vihrea1337.skycast.core.network

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import io.github.vihrea1337.skycast.core.network.retrofit.ForecastApi
import io.github.vihrea1337.skycast.core.network.retrofit.GeocodingApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitNetworkDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitNetworkDataSource

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        dataSource = RetrofitNetworkDataSource(
            forecastApi = retrofit.create(ForecastApi::class.java),
            geocodingApi = retrofit.create(GeocodingApi::class.java),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `real forecast sample is parsed and unknown fields are ignored`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast-response.json")))

        val result = getForecast()

        val data = (result as AppResult.Success).data
        assertEquals(21.4, data.current.temperature, 0.0)
        assertEquals(3, data.current.weatherCode)
        assertEquals(1, data.current.isDay)
        assertEquals(4, data.hourly.time.size)
        assertEquals(listOf(10, null, 20, 55), data.hourly.precipitationProbability)
        assertEquals(2, data.daily.time.size)
    }

    @Test
    fun `missing results field in geocoding yields empty list`() = runTest {
        server.enqueue(MockResponse().setBody("""{"generationtime_ms": 0.5}"""))

        val result = dataSource.searchPlaces(query = "Nowhere", language = "ru")

        assertEquals(emptyList<Any>(), (result as AppResult.Success).data)
    }

    @Test
    fun `http 500 maps to server error with code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = getForecast()

        assertEquals(AppError.Server(code = 500), (result as AppResult.Failure).error)
    }

    @Test
    fun `connection failure maps to network error`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = getForecast()

        assertEquals(AppError.Network, (result as AppResult.Failure).error)
    }

    @Test
    fun `broken contract maps to unknown error`() = runTest {
        server.enqueue(MockResponse().setBody("""{"current": "not an object"}"""))

        val result = getForecast()

        assertTrue((result as AppResult.Failure).error is AppError.Unknown)
    }

    private suspend fun getForecast() = dataSource.getForecast(
        latitude = 61.78491,
        longitude = 34.34691,
        temperatureUnit = TemperatureUnit.CELSIUS,
        windSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
    )

    private fun readResource(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "resource $name not found" }
            .readBytes()
            .decodeToString()
}
