import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Доменные модели — общий язык слоёв. Чистый Kotlin, без зависимостей (docs/PLAN.md §5.3)
plugins {
    alias(libs.plugins.kotlin.jvm)
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
    testImplementation(libs.junit)

    detektPlugins(libs.detekt.formatting)
}
