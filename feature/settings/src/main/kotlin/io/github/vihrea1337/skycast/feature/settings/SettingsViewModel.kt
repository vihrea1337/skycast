package io.github.vihrea1337.skycast.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
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
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    /**
     * Никакого локального стейта-копии: записали в DataStore → Flow вернул
     * новое значение → UI обновился. null — настройки ещё читаются.
     */
    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /**
     * Единицы конвертирует сервер (docs/PLAN.md §5.4), поэтому смена требует refresh.
     * Если сети нет — данные остаются в старых единицах со СТАРЫМИ подписями:
     * подписи берутся из units-at-sync, рассинхрона не будет (§5.12).
     * На этапе 5 сценарий переедет в use case.
     */
    fun onTemperatureUnitChange(unit: TemperatureUnit) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
            weatherRepository.refresh()
        }
    }

    fun onWindSpeedUnitChange(unit: WindSpeedUnit) {
        viewModelScope.launch {
            settingsRepository.setWindSpeedUnit(unit)
            weatherRepository.refresh()
        }
    }

    fun onThemeChange(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(mode) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
