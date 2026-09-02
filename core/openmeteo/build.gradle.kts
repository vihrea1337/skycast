import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Контракт ответа Open-Meteo `/v1/forecast` — общий язык между Android-клиентом
// (core:network) и опциональным серверным агрегатором (server/, Этап 9). Чистый
// Kotlin/JVM, без Android-зависимостей — оба модуля-потребителя могут его подключить,
// хотя один Android-библиотека, а другой обычный JVM-проект.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    detektPlugins(libs.detekt.formatting)
}
