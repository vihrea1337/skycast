package io.github.vihrea1337.skycast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.domain.settings.ObserveSettingsUseCase
import io.github.vihrea1337.skycast.core.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    /** Тема из настроек — применяется мгновенно на всём приложении. */
    val themeMode: StateFlow<ThemeMode> = observeSettings()
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ThemeMode.SYSTEM)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
