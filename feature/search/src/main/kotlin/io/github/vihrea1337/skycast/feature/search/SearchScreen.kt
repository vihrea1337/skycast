package io.github.vihrea1337.skycast.feature.search

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.designsystem.component.ErrorContent
import io.github.vihrea1337.skycast.core.model.Location

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val queryText by viewModel.queryText.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val saved by viewModel.savedLocations.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val isDetecting by viewModel.isDetecting.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect { onBack() }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onUseMyLocation() else viewModel.onPermissionDenied()
    }

    SearchScreen(
        queryText = queryText,
        results = results,
        saved = saved,
        message = message,
        isDetecting = isDetecting,
        onQueryChange = viewModel::onQueryChange,
        onResultClick = viewModel::onResultClick,
        onSavedClick = viewModel::onSavedClick,
        onDeleteSaved = viewModel::onDeleteSaved,
        onMessageShown = viewModel::onMessageShown,
        onBack = onBack,
        onMyLocationClick = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                viewModel.onUseMyLocation()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    queryText: String,
    results: SearchResultsState,
    saved: List<SavedLocationItem>,
    message: SearchMessage?,
    isDetecting: Boolean,
    onQueryChange: (String) -> Unit,
    onResultClick: (Location) -> Unit,
    onSavedClick: (Long) -> Unit,
    onDeleteSaved: (Long) -> Unit,
    onMessageShown: () -> Unit,
    onMyLocationClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = message?.text()
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_search_back),
                        )
                    }
                },
            )
            OutlinedTextField(
                value = queryText,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.feature_search_hint)) },
                singleLine = true,
            )
            when (results) {
                SearchResultsState.Idle -> SavedLocationsList(
                    saved = saved,
                    isDetecting = isDetecting,
                    onSavedClick = onSavedClick,
                    onDeleteSaved = onDeleteSaved,
                    onMyLocationClick = onMyLocationClick,
                )

                SearchResultsState.Searching -> CenteredProgress()

                SearchResultsState.Empty -> CenteredMessage(stringResource(R.string.feature_search_empty))

                is SearchResultsState.SearchError -> ErrorContent(
                    message = when (results.error) {
                        AppError.Network -> stringResource(R.string.feature_search_error_network)
                        else -> stringResource(R.string.feature_search_error_generic)
                    },
                    onRetry = { onQueryChange(queryText + " ") }, // меняем query → цепочка поиска перезапустится
                )

                is SearchResultsState.Results -> LazyColumn {
                    items(results.locations, key = { it.id }) { location ->
                        ListItem(
                            headlineContent = { Text(location.name) },
                            supportingContent = { Text(location.subtitle()) },
                            modifier = Modifier.clickableItem { onResultClick(location) },
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SavedLocationsList(
    saved: List<SavedLocationItem>,
    isDetecting: Boolean,
    onSavedClick: (Long) -> Unit,
    onDeleteSaved: (Long) -> Unit,
    onMyLocationClick: () -> Unit,
) {
    LazyColumn {
        item(key = "my_location") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.feature_search_my_location)) },
                leadingContent = {
                    if (isDetecting) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    }
                },
                modifier = Modifier.clickableItem(enabled = !isDetecting, onClick = onMyLocationClick),
            )
        }
        items(saved, key = { it.location.id }) { item ->
            ListItem(
                headlineContent = { Text(item.location.name) },
                supportingContent = { Text(item.location.subtitle()) },
                leadingContent = {
                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.feature_search_selected),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onDeleteSaved(item.location.id) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.feature_search_delete),
                        )
                    }
                },
                modifier = Modifier.clickableItem { onSavedClick(item.location.id) },
            )
        }
    }
}

@Composable
private fun CenteredProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
        )
    }
}

private fun Location.subtitle(): String = listOfNotNull(region, country).joinToString(", ")

@Composable
private fun SearchMessage.text(): String = when (this) {
    SearchMessage.PERMISSION_DENIED -> stringResource(R.string.feature_search_permission_denied)
    SearchMessage.DETECT_FAILED -> stringResource(R.string.feature_search_detect_failed)
}

private fun Modifier.clickableItem(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    this.clickable(enabled = enabled, onClick = onClick)
