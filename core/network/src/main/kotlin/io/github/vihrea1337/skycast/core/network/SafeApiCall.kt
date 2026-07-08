package io.github.vihrea1337.skycast.core.network

import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Граница обработки исключений data-слоя: выше летят только типизированные [AppError].
 * [CancellationException] обязана пролетать насквозь — иначе ломается отмена корутин.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    AppResult.Failure(AppError.Network)
} catch (e: HttpException) {
    AppResult.Failure(AppError.Server(e.code()))
} catch (e: Exception) {
    // Сюда попадают в т.ч. SerializationException — признак расхождения контракта API
    AppResult.Failure(AppError.Unknown)
}
