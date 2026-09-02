package io.github.vihrea1337.skycast.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * Мини-бэкенд SkyCast: агрегирует Open-Meteo в один аккуратный JSON и документирует
 * себя через Swagger UI на /swagger. Отдельный от Android-приложения проект — общий
 * только контракт данных (те же поля, что и в мобильном клиенте, см. OpenMeteoDto.kt).
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8090
    val host = System.getenv("HOST") ?: "0.0.0.0"
    embeddedServer(Netty, port = port, host = host, module = Application::module).start(wait = true)
}

fun Application.module() {
    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        // Без явного таймаута зависший/медленный Open-Meteo подвесил бы запрос
        // клиента без всякой границы — проверено на практике: в контейнере
        // холодное DNS-разрешение иногда занимает несколько секунд.
        install(HttpTimeout) {
            requestTimeoutMillis = 12_000
            connectTimeoutMillis = 6_000
        }
    }
    // Закрываем HTTP-клиент при остановке сервера — иначе поток CIO-движка утекает
    // (заметно в тестах, которые поднимают/гасят сервер много раз подряд).
    monitor.subscribe(ApplicationStopped) { httpClient.close() }

    configure(WeatherService(OpenMeteoClient(httpClient), WeatherCache()))
}

/**
 * Плагины и маршруты — отдельно от сборки реального HttpClient(CIO), чтобы
 * [WeatherServiceTest]-соседи по духу — [ApplicationTest] — могли поднять настоящий
 * Ktor-роутинг с фейковым [WeatherService] и без единого похода в интернет.
 */
fun Application.configure(weatherService: WeatherService) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<InvalidCoordinatesException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Invalid coordinates"))
        }
        exception<NumberFormatException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("lat/lon должны быть числами"))
        }
        // Open-Meteo не ответил вовремя (см. HttpTimeout у клиента ниже) — 503, а не
        // зависший запрос без ответа: у клиента SkyCast/curl всегда будет чёткая граница.
        exception<HttpRequestTimeoutException> { call, _ ->
            call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("Open-Meteo не ответил вовремя, попробуйте ещё раз"))
        }
    }
    // Наивный per-IP лимит: мы сами ходим в Open-Meteo от имени клиента, и у Open-Meteo
    // есть общая бесплатная квота на весь сервер — один клиент не должен иметь возможность
    // вычерпать её для всех остальных. Ключ по умолчанию — IP отправителя запроса.
    install(RateLimit) {
        register(RateLimitName("weather")) {
            rateLimiter(limit = 20, refillPeriod = 60.seconds)
            // Без явного requestKey все запросы делят один и тот же бакет на весь
            // процесс — нужен именно per-IP лимит, поэтому ключ задаём сами.
            // ВАЖНО: remoteAddress, не remoteHost! remoteHost — это
            // InetAddress.getHostName(), который делает обратный DNS-запрос на каждый
            // вызов; в Docker-сети для внутреннего IP это зависает на секунды —
            // проверено на практике (каждый запрос из-за этого занимал ~8с).
            requestKey { call -> call.request.origin.remoteAddress }
        }
    }

    routing {
        // Тот же контракт, что и у devlog/expenses — единообразная проверка живости.
        get("/health") {
            call.respondText("""{"status":"ok"}""", ContentType.Application.Json)
        }

        rateLimit(RateLimitName("weather")) {
            get("/api/v1/weather") {
                val lat = call.request.queryParameters["lat"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("параметр lat обязателен"))
                val lon = call.request.queryParameters["lon"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("параметр lon обязателен"))
                val weather = weatherService.getWeather(lat.toDouble(), lon.toDouble())
                call.respond(weather)
            }
        }

        // Swagger UI на /swagger, спека — resources/openapi/documentation.yaml.
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}

@Serializable
data class ErrorResponse(val error: String)
