package io.github.vihrea1337.skycast.core.data.testdouble

import io.github.vihrea1337.skycast.core.database.dao.LocationDao
import io.github.vihrea1337.skycast.core.database.dao.WeatherDao
import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake вместо mock (docs/PLAN.md §6.3): поведение в памяти на MutableStateFlow,
 * транзакционные методы приходят готовыми из default-реализаций интерфейса.
 * Инварианты самих SQL-запросов проверяет DaoTest в :core:database.
 */
class FakeWeatherDao : WeatherDao {

    private val current = MutableStateFlow<Map<Long, CurrentWeatherEntity>>(emptyMap())
    private val hourly = MutableStateFlow<Map<Long, List<HourlyForecastEntity>>>(emptyMap())
    private val daily = MutableStateFlow<Map<Long, List<DailyForecastEntity>>>(emptyMap())

    override fun observeCurrent(locationId: Long): Flow<CurrentWeatherEntity?> =
        current.map { it[locationId] }

    override fun observeHourly(locationId: Long): Flow<List<HourlyForecastEntity>> =
        hourly.map { it[locationId].orEmpty() }

    override fun observeDaily(locationId: Long): Flow<List<DailyForecastEntity>> =
        daily.map { it[locationId].orEmpty() }

    override suspend fun upsertCurrent(current: CurrentWeatherEntity) {
        this.current.update { it + (current.locationId to current) }
    }

    override suspend fun insertHourly(items: List<HourlyForecastEntity>) {
        val locationId = items.firstOrNull()?.locationId ?: return
        hourly.update { it + (locationId to it[locationId].orEmpty() + items) }
    }

    override suspend fun insertDaily(items: List<DailyForecastEntity>) {
        val locationId = items.firstOrNull()?.locationId ?: return
        daily.update { it + (locationId to it[locationId].orEmpty() + items) }
    }

    override suspend fun deleteHourly(locationId: Long) {
        hourly.update { it - locationId }
    }

    override suspend fun deleteDaily(locationId: Long) {
        daily.update { it - locationId }
    }
}

class FakeLocationDao : LocationDao {

    private val locations = MutableStateFlow<List<LocationEntity>>(emptyList())

    override fun observeAll(): Flow<List<LocationEntity>> =
        locations.map { list -> list.sortedBy { it.sortOrder } }

    override fun observeSelected(): Flow<LocationEntity?> =
        locations.map { list -> list.firstOrNull { it.isSelected } }

    override suspend fun upsert(location: LocationEntity) {
        locations.update { list -> list.filterNot { it.id == location.id } + location }
    }

    override suspend fun delete(id: Long) {
        locations.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun clearSelection() {
        locations.update { list -> list.map { it.copy(isSelected = false) } }
    }

    override suspend fun setSelected(id: Long) {
        locations.update { list -> list.map { if (it.id == id) it.copy(isSelected = true) else it } }
    }

    override suspend fun nextSortOrder(): Int =
        (locations.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
}
