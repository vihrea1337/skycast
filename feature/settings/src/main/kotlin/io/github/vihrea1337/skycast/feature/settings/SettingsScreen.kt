package io.github.vihrea1337.skycast.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onTemperatureUnitChange = viewModel::onTemperatureUnitChange,
        onWindSpeedUnitChange = viewModel::onWindSpeedUnitChange,
        onThemeChange = viewModel::onThemeChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    settings: UserSettings?,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onWindSpeedUnitChange: (WindSpeedUnit) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.feature_settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.feature_settings_back),
                    )
                }
            },
        )
        if (settings == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SectionTitle(stringResource(R.string.feature_settings_units))

            SettingLabel(stringResource(R.string.feature_settings_temperature))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TemperatureUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = settings.temperatureUnit == unit,
                        onClick = { onTemperatureUnitChange(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index, TemperatureUnit.entries.size),
                    ) {
                        Text(unit.label())
                    }
                }
            }

            SettingLabel(stringResource(R.string.feature_settings_wind_speed))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WindSpeedUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = settings.windSpeedUnit == unit,
                        onClick = { onWindSpeedUnitChange(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index, WindSpeedUnit.entries.size),
                    ) {
                        Text(unit.label())
                    }
                }
            }

            SectionTitle(stringResource(R.string.feature_settings_appearance))
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(mode) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = settings.theme == mode,
                        onClick = { onThemeChange(mode) },
                    )
                    Text(mode.label())
                }
            }

            SectionTitle(stringResource(R.string.feature_settings_about))
            AboutLink(
                text = stringResource(R.string.feature_settings_attribution),
                url = "https://open-meteo.com/",
            )
            AboutLink(
                text = stringResource(R.string.feature_settings_source_code),
                url = "https://github.com/vihrea1337/skycast",
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun AboutLink(text: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(url) }
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun TemperatureUnit.label(): String = when (this) {
    TemperatureUnit.CELSIUS -> stringResource(R.string.feature_settings_celsius)
    TemperatureUnit.FAHRENHEIT -> stringResource(R.string.feature_settings_fahrenheit)
}

@Composable
private fun WindSpeedUnit.label(): String = when (this) {
    WindSpeedUnit.METERS_PER_SECOND -> stringResource(R.string.feature_settings_ms)
    WindSpeedUnit.KILOMETERS_PER_HOUR -> stringResource(R.string.feature_settings_kmh)
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.feature_settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.feature_settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.feature_settings_theme_dark)
}
