package io.github.vihrea1337.skycast.feature.home

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.model.WeatherData

/**
 * Состояния Home-экрана (этап 2: без кэша).
 * На этапе 3 Error получит поле cached, а Content — isRefreshing (docs/PLAN.md §5.10).
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val weather: WeatherData) : HomeUiState
    data class Error(val error: AppError) : HomeUiState
}
