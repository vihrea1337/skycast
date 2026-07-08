package io.github.vihrea1337.skycast.feature.home

/**
 * Состояния Home-экрана.
 *
 * Этап 1 — только плейсхолдер; реальный набор (Loading / NoCity / Content / Error)
 * появится на этапе 2 вместе с данными (docs/PLAN.md §5.10).
 */
sealed interface HomeUiState {
    data object Placeholder : HomeUiState
}
