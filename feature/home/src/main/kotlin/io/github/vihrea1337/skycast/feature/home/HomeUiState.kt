package io.github.vihrea1337.skycast.feature.home

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.model.WeatherData

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val weather: WeatherData,
        val isRefreshing: Boolean,
        /** Ошибка при живом кэше: показываем snackbar, данные остаются на экране. */
        val transientError: AppError?,
    ) : HomeUiState

    /** Полноэкранная ошибка — только когда кэша нет вообще (docs/PLAN.md §2.6). */
    data class Error(val error: AppError) : HomeUiState
}
