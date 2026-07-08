package io.github.vihrea1337.skycast.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    /** Плейсхолдер: на этапе 2 сюда придёт ObserveWeatherUseCase + stateIn (docs/PLAN.md §5.10). */
    val uiState: StateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Placeholder)
}
