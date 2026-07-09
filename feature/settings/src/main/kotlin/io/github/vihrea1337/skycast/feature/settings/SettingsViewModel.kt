package io.github.vihrea1337.skycast.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.domain.settings.ObserveSettingsUseCase
import io.github.vihrea1337.skycast.core.domain.settings.UpdateSettingsUseCase
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
) : ViewModel() {

    /**
     * Никакого локального стейта-копии: записали в DataStore → Flow вернул
     * новое значение → UI обновился. null — настройки ещё читаются.
     */
    val settings: StateFlow<UserSettings?> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun onTemperatureUnitChange(unit: TemperatureUnit) {
        viewModelScope.launch { updateSettings.temperatureUnit(unit) }
    }

    fun onWindSpeedUnitChange(unit: WindSpeedUnit) {
        viewModelScope.launch { updateSettings.windSpeedUnit(unit) }
    }

    fun onThemeChange(mode: ThemeMode) {
        viewModelScope.launch { updateSettings.theme(mode) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
