package io.github.vihrea1337.skycast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme

/** Stateful-обвязка: связывает ViewModel и stateless-экран (конвенция из docs/PLAN.md §2.7). */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = uiState, modifier = modifier)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        HomeUiState.Placeholder -> PlaceholderContent(modifier)
    }
}

@Composable
private fun PlaceholderContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_home_placeholder_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.feature_home_placeholder_subtitle),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SkyCastTheme {
        HomeScreen(state = HomeUiState.Placeholder)
    }
}
