package io.github.vihrea1337.skycast.core.common.result

/**
 * Типизированный результат операций data-слоя.
 *
 * Вместо [kotlin.Result]: тот не типизирует ошибку (только Throwable),
 * а UI должен различать «нет сети» и «сервер упал» без каскадов instanceof.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/** Все ошибки, которые data-слой показывает наружу. Исключения выше data-слоя не летают. */
sealed interface AppError {
    /** Нет сети или таймаут (IOException). */
    data object Network : AppError

    /** Сервер ответил не-2xx. */
    data class Server(val code: Int) : AppError

    /** Всё остальное, включая ошибки парсинга (расхождение контракта API). */
    data object Unknown : AppError
}
