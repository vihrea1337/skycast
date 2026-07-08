package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.concurrency.Dispatcher
import io.github.vihrea1337.skycast.core.common.concurrency.DispatcherType
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.mapper.toWeatherData
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject

internal class DefaultWeatherRepository @Inject constructor(
    private val network: NetworkDataSource,
    private val clock: Clock,
    @Dispatcher(DispatcherType.Default) private val defaultDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    override suspend fun getWeather(
        location: Location,
        settings: UserSettings,
    ): AppResult<WeatherData> = withContext(defaultDispatcher) {
        val response = network.getForecast(
            latitude = location.latitude,
            longitude = location.longitude,
            temperatureUnit = settings.temperatureUnit,
            windSpeedUnit = settings.windSpeedUnit,
        )
        when (response) {
            is AppResult.Failure -> response
            is AppResult.Success -> mapSafely(response) { dto ->
                dto.toWeatherData(location = location, updatedAt = clock.instant())
            }
        }
    }

    /** Ошибка маппинга = расхождение контракта API, а не крэш приложения. */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private inline fun <T, R> mapSafely(result: AppResult.Success<T>, transform: (T) -> R): AppResult<R> =
        try {
            AppResult.Success(transform(result.data))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown)
        }
}
