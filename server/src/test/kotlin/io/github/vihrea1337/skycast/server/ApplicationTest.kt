package io.github.vihrea1337.skycast.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Клиент, который падает при любой реальной попытке сходить в сеть — тесты его не должны трогать. */
private fun throwingHttpClient() = HttpClient(MockEngine) {
    engine {
        addHandler { error("Тест не должен был обращаться к сети: WeatherCache уже прогрет") }
    }
}

/**
 * Поднимает настоящий Ktor-роутинг (плагины, StatusPages, RateLimit — всё как в проде)
 * через [configure], но с фейковым [WeatherService] вместо реального HTTP-клиента
 * к Open-Meteo — ни одного похода в интернет.
 *
 * Этот тест существует из-за реального бага, найденного при ручной проверке: лимитер
 * без явного requestKey либо не группировал запросы вовсе, либо (после первой попытки
 * исправления через `origin.remoteHost`) на каждый запрос делал обратный DNS-lookup и
 * вешал сервер на секунды. Без автоматического теста такая регрессия осталась бы
 * незамеченной до следующей ручной проверки в Docker.
 */
class ApplicationTest {

    private fun fakeWeatherService() = WeatherService(
        openMeteo = OpenMeteoClient(httpClient = throwingHttpClient()),
        cache = WeatherCache(),
    )

    @Test
    fun `health responds ok without touching the rate limiter`() = testApplication {
        application { configure(fakeWeatherService()) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ok"}""", response.bodyAsText())
    }

    @Test
    fun `missing query params return 400, not 500`() = testApplication {
        application { configure(fakeWeatherService()) }
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/weather").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/weather?lat=1").status)
    }

    @Test
    fun `21st request from the same client within the window is rate limited`() = testApplication {
        // Кэш прогрет напрямую (без сети), чтобы каждый вызов маршрута был мгновенным
        // и не требовал реального HttpClient — проверяем именно лимитер, не Open-Meteo.
        val cache = WeatherCache()
        cache.put(61.785, 34.347, sampleResponse())
        val service = WeatherService(openMeteo = OpenMeteoClient(throwingHttpClient()), cache = cache)
        application { configure(service) }

        val statuses = (1..21).map {
            client.get("/api/v1/weather?lat=61.785&lon=34.347").status
        }

        assertTrue(statuses.take(20).all { it == HttpStatusCode.OK }, "первые 20 запросов должны пройти: $statuses")
        assertEquals(HttpStatusCode.TooManyRequests, statuses[20], "21-й запрос должен упереться в лимит: $statuses")
    }

    private fun sampleResponse() = AggregatedWeatherResponse(
        latitude = 61.785,
        longitude = 34.347,
        current = CurrentWeather(
            time = "2026-09-02T12:00", temperatureC = 20.0, feelsLikeC = 19.0,
            humidityPercent = 60, weatherCode = 1, windSpeedKmh = 5.0, pressureHpa = 1000.0, isDay = true,
        ),
        hourly = emptyList(),
        daily = emptyList(),
        generatedAt = "2026-09-02T12:00:00Z",
        cached = false,
    )
}
