package io.github.vihrea1337.skycast.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
    }

    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val weatherService = WeatherService(OpenMeteoClient(httpClient), WeatherCache())

    routing {
        // Тот же контракт, что и у devlog/expenses — единообразная проверка живости.
        get("/health") {
            call.respondText("""{"status":"ok"}""", ContentType.Application.Json)
        }

        get("/api/v1/weather") {
            val lat = call.request.queryParameters["lat"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("параметр lat обязателен"))
            val lon = call.request.queryParameters["lon"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("параметр lon обязателен"))
            val weather = weatherService.getWeather(lat.toDouble(), lon.toDouble())
            call.respond(weather)
        }

        // Swagger UI на /swagger, спека — resources/openapi/documentation.yaml.
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}

@Serializable
data class ErrorResponse(val error: String)
