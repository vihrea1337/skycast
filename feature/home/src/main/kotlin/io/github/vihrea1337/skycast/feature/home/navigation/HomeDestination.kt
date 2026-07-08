package io.github.vihrea1337.skycast.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.vihrea1337.skycast.feature.home.HomeRoute
import kotlinx.serialization.Serializable

/** Type-safe маршрут Home-экрана (Navigation Compose + kotlinx.serialization). */
@Serializable
data object HomeDestination

/**
 * Точка подключения фичи к NavHost.
 * Колбэки навигации (поиск, настройки) добавятся вместе с соответствующими фичами.
 */
fun NavGraphBuilder.homeScreen() {
    composable<HomeDestination> {
        HomeRoute()
    }
}
