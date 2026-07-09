import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Use cases — тонкая прослойка между репозиториями и ViewModel (docs/PLAN.md §5.8).
// Android library только из-за зависимости от :core:data; сам код — чистый Kotlin.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
}

android {
    namespace = "io.github.vihrea1337.skycast.core.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
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
    // Фичи получают модели и AppResult через domain и не зависят от data напрямую
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:data"))

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    detektPlugins(libs.detekt.formatting)
}
