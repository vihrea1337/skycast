package io.github.vihrea1337.skycast.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Проверяет саму суть сервера — превращение сырых параллельных массивов Open-Meteo
 * в удобный список объектов — без единого реального похода в сеть (MockEngine).
 */
class WeatherServiceTest {

    // Пример сырого ответа Open-Meteo: 2 часа, 2 дня — достаточно, чтобы проверить сшивку.
    private val sampleOpenMeteoJson = """
        {
          "current": {
            "time": "2026-09-02T14:00", "temperature_2m": 21.4, "apparent_temperature": 19.8,
            "relative_humidity_2m": 62, "weather_code": 3, "wind_speed_10m": 4.2,
            "surface_pressure": 1006.9, "is_day": 1
          },
          "hourly": {
            "time": ["2026-09-02T14:00", "2026-09-02T15:00"],
            "temperature_2m": [21.4, 20.9],
            "weather_code": [3, 61],
            "precipitation_probability": [10, null],
            "is_day": [1, 0]
          },
          "daily": {
            "time": ["2026-09-02", "2026-09-03"],
            "weather_code": [3, 61],
            "temperature_2m_max": [22.0, 18.0],
            "temperature_2m_min": [14.0, 12.0],
            "sunrise": ["2026-09-02T05:12", "2026-09-03T05:14"],
            "sunset": ["2026-09-02T19:47", "2026-09-03T19:44"]
          }
        }
    """.trimIndent()

    private fun serviceWithMockedOpenMeteo(): WeatherService {
        val mockEngine = MockEngine { _ ->
            respond(
                content = sampleOpenMeteoJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        return WeatherService(OpenMeteoClient(httpClient), WeatherCache())
    }

    @Test
    fun `aggregates parallel arrays into a list of objects`() = runTest {
        val result = serviceWithMockedOpenMeteo().getWeather(61.785, 34.347)

        assertEquals(2, result.hourly.size)
        assertEquals(2, result.daily.size)
        assertFalse(result.cached)

        assertEquals("2026-09-02T15:00", result.hourly[1].time)
        assertEquals(20.9, result.hourly[1].temperatureC)
        assertEquals(null, result.hourly[1].precipitationProbabilityPercent, "null в исходном массиве должен остаться null")
        assertFalse(result.hourly[1].isDay)

        assertEquals("2026-09-03", result.daily[1].date)
        assertEquals(18.0, result.daily[1].temperatureMaxC)

        assertTrue(result.current.isDay)
        assertEquals(21.4, result.current.temperatureC)
    }

    @Test
    fun `second call for the same coordinates is served from cache`() = runTest {
        var requestCount = 0
        val mockEngine = MockEngine { _ ->
            requestCount++
            respond(sampleOpenMeteoJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val httpClient = HttpClient(mockEngine) { install(ContentNegotiation) { json() } }
        val service = WeatherService(OpenMeteoClient(httpClient), WeatherCache())

        val first = service.getWeather(61.785, 34.347)
        val second = service.getWeather(61.785, 34.347)

        assertEquals(1, requestCount, "второй запрос не должен ходить в Open-Meteo снова")
        assertFalse(first.cached)
        assertTrue(second.cached)
    }

    @Test
    fun `rejects out-of-range latitude`() = runTest {
        assertFailsWith<InvalidCoordinatesException> {
            serviceWithMockedOpenMeteo().getWeather(latitude = 120.0, longitude = 34.347)
        }
    }

    @Test
    fun `rejects out-of-range longitude`() = runTest {
        assertFailsWith<InvalidCoordinatesException> {
            serviceWithMockedOpenMeteo().getWeather(latitude = 61.785, longitude = 200.0)
        }
    }
}
