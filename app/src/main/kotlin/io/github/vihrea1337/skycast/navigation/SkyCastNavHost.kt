package io.github.vihrea1337.skycast.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.vihrea1337.skycast.feature.home.navigation.HomeDestination
import io.github.vihrea1337.skycast.feature.home.navigation.homeScreen
import io.github.vihrea1337.skycast.feature.search.navigation.SearchDestination
import io.github.vihrea1337.skycast.feature.search.navigation.searchScreen
import io.github.vihrea1337.skycast.feature.settings.navigation.SettingsDestination
import io.github.vihrea1337.skycast.feature.settings.navigation.settingsScreen

/**
 * Единственное место, где фичи знают друг о друге: навигация между ними
 * собирается здесь через колбэки (docs/PLAN.md §5.1).
 */
@Composable
fun SkyCastNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onSearchClick = { navController.navigate(SearchDestination) },
            onSettingsClick = { navController.navigate(SettingsDestination) },
        )
        searchScreen(onBack = navController::popBackStack)
        settingsScreen(onBack = navController::popBackStack)
    }
}
