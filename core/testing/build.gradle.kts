import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Общий тестовый код: MainDispatcherRule, fake-репозитории, сэмплы моделей (docs/PLAN.md §6.3)
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
}

android {
    namespace = "io.github.vihrea1337.skycast.core.testing"
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
    api(project(":core:data"))
    api(project(":core:model"))
    api(project(":core:common"))

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.coroutines.core)

    detektPlugins(libs.detekt.formatting)
}
