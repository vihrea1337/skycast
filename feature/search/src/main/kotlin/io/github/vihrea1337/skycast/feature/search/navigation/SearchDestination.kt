package io.github.vihrea1337.skycast.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.vihrea1337.skycast.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchDestination

fun NavGraphBuilder.searchScreen(onBack: () -> Unit) {
    composable<SearchDestination> {
        SearchRoute(onBack = onBack)
    }
}
