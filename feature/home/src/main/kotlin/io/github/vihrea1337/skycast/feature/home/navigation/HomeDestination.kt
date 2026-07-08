package io.github.vihrea1337.skycast.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.vihrea1337.skycast.feature.home.HomeRoute
import kotlinx.serialization.Serializable

/** Type-safe маршрут Home-экрана (Navigation Compose + kotlinx.serialization). */
@Serializable
data object HomeDestination

/** Точка подключения фичи к NavHost. Куда ведут колбэки — решает :app (docs/PLAN.md §5.1). */
fun NavGraphBuilder.homeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    composable<HomeDestination> {
        HomeRoute(onSearchClick = onSearchClick, onSettingsClick = onSettingsClick)
    }
}
