import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Чистый JVM-модуль: без Android SDK — быстрые тесты и честные границы слоёв (docs/PLAN.md §5.2)
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
    api(libs.javax.inject)

    detektPlugins(libs.detekt.formatting)
}
