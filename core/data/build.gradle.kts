import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Сердце data-слоя: репозитории + мапперы DTO → доменные модели (docs/PLAN.md §5.7)
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

android {
    namespace = "io.github.vihrea1337.skycast.core.data"
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
    api(project(":core:model"))
    api(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:openmeteo"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.location)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    detektPlugins(libs.detekt.formatting)
}
