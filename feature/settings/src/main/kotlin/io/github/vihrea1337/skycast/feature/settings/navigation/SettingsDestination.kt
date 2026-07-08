package io.github.vihrea1337.skycast.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.vihrea1337.skycast.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsDestination

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsDestination> {
        SettingsRoute(onBack = onBack)
    }
}
