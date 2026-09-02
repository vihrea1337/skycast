pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SkyCast"
include(":app")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:domain")
include(":core:designsystem")
include(":core:testing")
include(":feature:home")
include(":feature:search")
include(":feature:settings")
include(":widget")
// Мини-бэкенд на Ktor (Этап 9, опционально) — агрегатор Open-Meteo со Swagger UI.
// Обычный JVM-модуль, Android-плагины не трогает. Подробности — docs/03-api-swagger.md.
include(":server")
