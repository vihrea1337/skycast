plugins {
    // "application" — умеет запускать сервер и собирать его в исполняемый вид.
    application
    // Kotlin для JVM (не Android) — этот модуль не относится к Android-сборке SkyCast.
    alias(libs.plugins.kotlin.jvm)
    // kotlinx.serialization — учит компилятор превращать классы в JSON и обратно.
    alias(libs.plugins.kotlin.serialization)
    // Плагин Ktor — удобный запуск и сборка "толстого" JAR (один файл для Docker-образа).
    id("io.ktor.plugin") version "3.0.3"
}

group = "io.github.vihrea1337.skycast.server"
version = "0.1.0"

application {
    // Файл Application.kt Kotlin превращает в класс с именем ApplicationKt.
    mainClass.set("io.github.vihrea1337.skycast.server.ApplicationKt")
}

kotlin {
    // Тот же JDK 21, что и у остальных бэкендов пользователя (devlog, expenses).
    jvmToolchain(21)
}

// Репозитории берутся из корневого settings.gradle.kts (dependencyResolutionManagement,
// FAIL_ON_PROJECT_REPOS) — google() + mavenCentral() уже хватает для всех зависимостей ниже.

dependencies {
    // Контракт ответа Open-Meteo — тот же класс, что использует Android-приложение
    // (core:network). Чисто-JVM модуль, без Android-зависимостей — можно подключать
    // напрямую. См. docs/03-api-swagger.md.
    implementation(project(":core:openmeteo"))

    // --- Сервер: принимает запрос от клиента SkyCast (или curl/Swagger UI) ---
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    // Единая обработка ошибок → аккуратные JSON вместо стектрейса на неверных координатах.
    implementation("io.ktor:ktor-server-status-pages")
    // Наивный per-IP rate limit — публичный эндпоинт проксирует Open-Meteo, один клиент
    // не должен иметь возможность вычерпать общую квоту сервиса на всех.
    implementation("io.ktor:ktor-server-rate-limit")
    // Swagger UI на /swagger, читает статический OpenAPI-файл из resources/openapi/.
    implementation("io.ktor:ktor-server-swagger")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // --- Клиент: сам сервер ходит в Open-Meteo, чтобы агрегировать ответ ---
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.ktor:ktor-client-mock")
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
