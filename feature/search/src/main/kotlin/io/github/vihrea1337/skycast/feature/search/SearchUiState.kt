package io.github.vihrea1337.skycast.feature.search

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.model.Location

sealed interface SearchResultsState {
    /** Запрос пуст или короче 2 символов — показываем сохранённые города. */
    data object Idle : SearchResultsState
    data object Searching : SearchResultsState
    data class Results(val locations: List<Location>) : SearchResultsState
    data object Empty : SearchResultsState
    data class SearchError(val error: AppError) : SearchResultsState
}

data class SavedLocationItem(
    val location: Location,
    val isSelected: Boolean,
)

/** Одноразовые сообщения для snackbar. */
enum class SearchMessage {
    PERMISSION_DENIED,
    DETECT_FAILED,
}
