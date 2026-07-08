package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.concurrency.Dispatcher
import io.github.vihrea1337.skycast.core.common.concurrency.DispatcherType
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.location.CurrentPositionProvider
import io.github.vihrea1337.skycast.core.data.mapper.toEntity
import io.github.vihrea1337.skycast.core.data.mapper.toLocation
import io.github.vihrea1337.skycast.core.database.dao.LocationDao
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.network.NetworkDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

internal class DefaultLocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val network: NetworkDataSource,
    private val positionProvider: CurrentPositionProvider,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
) : LocationRepository {

    override fun observeSavedLocations(): Flow<List<Location>> =
        locationDao.observeAll().map { list -> list.map { it.toLocation() } }

    override fun observeSelected(): Flow<Location?> =
        locationDao.observeSelected().map { it?.toLocation() }

    override suspend fun search(query: String): AppResult<List<Location>> =
        when (val result = network.searchPlaces(query, language = appLanguage())) {
            is AppResult.Failure -> result
            is AppResult.Success -> AppResult.Success(result.data.map { it.toLocation() })
        }

    override suspend fun saveAndSelect(location: Location) {
        locationDao.saveAndSelect(location.toEntity(isSelected = true))
    }

    override suspend fun select(id: Long) {
        locationDao.select(id)
    }

    override suspend fun delete(id: Long) {
        val wasSelected = locationDao.observeSelected().first()?.id == id
        locationDao.delete(id)
        if (wasSelected) {
            locationDao.observeAll().first().firstOrNull()?.let { locationDao.select(it.id) }
        }
    }

    /**
     * Координаты (Fused, coarse) → название города (Geocoder) → канонический город
     * из geocoding API: ближайший к координатам из найденных.
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    override suspend fun detectCurrent(): AppResult<Location> = withContext(ioDispatcher) {
        try {
            val position = positionProvider.currentPosition()
                ?: return@withContext AppResult.Failure(AppError.Unknown)
            val locality = positionProvider.localityName(position)
                ?: return@withContext AppResult.Failure(AppError.Unknown)

            when (val results = network.searchPlaces(locality, language = appLanguage())) {
                is AppResult.Failure -> results
                is AppResult.Success ->
                    results.data
                        .minByOrNull { squaredDistance(it.latitude, it.longitude, position) }
                        ?.let { AppResult.Success(it.toLocation()) }
                        ?: AppResult.Failure(AppError.Unknown)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown)
        }
    }

    /** Квадрат евклидова расстояния — достаточно, чтобы выбрать ближайший из пяти результатов. */
    private fun squaredDistance(
        latitude: Double,
        longitude: Double,
        position: CurrentPositionProvider.Position,
    ): Double {
        val dLat = latitude - position.latitude
        val dLon = longitude - position.longitude
        return dLat * dLat + dLon * dLon
    }

    private fun appLanguage(): String = Locale.getDefault().language.ifBlank { "en" }
}
