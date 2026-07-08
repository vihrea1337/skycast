package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.concurrency.Dispatcher
import io.github.vihrea1337.skycast.core.common.concurrency.DispatcherType
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.mapper.toCurrentWeatherEntity
import io.github.vihrea1337.skycast.core.data.mapper.toDailyForecastEntities
import io.github.vihrea1337.skycast.core.data.mapper.toHourlyForecastEntities
import io.github.vihrea1337.skycast.core.data.mapper.toWeatherData
import io.github.vihrea1337.skycast.core.database.dao.LocationDao
import io.github.vihrea1337.skycast.core.database.dao.WeatherDao
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject

internal class DefaultWeatherRepository @Inject constructor(
    private val network: NetworkDataSource,
    private val weatherDao: WeatherDao,
    private val locationDao: LocationDao,
    private val clock: Clock,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    /**
     * SSOT: читаем только из Room. flatMapLatest — смена выбранного города
     * автоматически переключает поток погоды (docs/PLAN.md §5.7).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeWeather(): Flow<WeatherData?> =
        locationDao.observeSelected()
            .flatMapLatest { selected ->
                if (selected == null) {
                    flowOf(null)
                } else {
                    combine(
                        weatherDao.observeCurrent(selected.id),
                        weatherDao.observeHourly(selected.id),
                        weatherDao.observeDaily(selected.id),
                    ) { current, hourly, daily ->
                        current?.let { toWeatherData(selected, it, hourly, daily) }
                    }
                }
            }
            .flowOn(ioDispatcher)

    override suspend fun refresh(): AppResult<Unit> = withContext(ioDispatcher) {
        val location = locationDao.observeSelected().first() ?: seedDefaultLocation()
        val settings = UserSettings.Default // этап 4: единицы придут из DataStore

        val response = network.getForecast(
            latitude = location.latitude,
            longitude = location.longitude,
            temperatureUnit = settings.temperatureUnit,
            windSpeedUnit = settings.windSpeedUnit,
        )
        when (response) {
            is AppResult.Failure -> response
            is AppResult.Success -> storeSafely {
                val dto = response.data
                weatherDao.replaceForecast(
                    locationId = location.id,
                    current = dto.toCurrentWeatherEntity(location.id, settings, updatedAt = clock.instant()),
                    hourly = dto.hourly.toHourlyForecastEntities(location.id, currentTime = dto.current.time),
                    daily = dto.daily.toDailyForecastEntities(location.id),
                )
            }
        }
    }

    /** Ошибка маппинга/записи = расхождение контракта API; кэш остаётся прежним. */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun storeSafely(store: suspend () -> Unit): AppResult<Unit> = try {
        store()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown)
    }

    /** До появления :feature:search (этап 4) первый запуск сеет дефолтный город. */
    private suspend fun seedDefaultLocation(): LocationEntity {
        locationDao.saveAndSelect(DefaultLocation)
        return DefaultLocation.copy(isSelected = true)
    }

    companion object {
        internal val DefaultLocation = LocationEntity(
            id = 509820,
            name = "Петрозаводск",
            region = "Карелия",
            country = "Россия",
            latitude = 61.78491,
            longitude = 34.34691,
            isSelected = true,
            sortOrder = 0,
        )
    }
}
