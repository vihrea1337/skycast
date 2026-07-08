package io.github.vihrea1337.skycast.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.vihrea1337.skycast.feature.home.navigation.HomeDestination
import io.github.vihrea1337.skycast.feature.home.navigation.homeScreen

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
        homeScreen()
    }
}
