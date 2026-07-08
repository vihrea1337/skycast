package io.github.vihrea1337.skycast.core.data.repository

import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    fun observeSavedLocations(): Flow<List<Location>>

    fun observeSelected(): Flow<Location?>

    /** Геокодинг по названию. Результаты НЕ сохраняются, пока пользователь не выбрал. */
    suspend fun search(query: String): AppResult<List<Location>>

    /** Сохранить город (в конец списка) и сразу сделать его выбранным. */
    suspend fun saveAndSelect(location: Location)

    suspend fun select(id: Long)

    /** Удаление выбранного города переназначает выбор на первый оставшийся. */
    suspend fun delete(id: Long)

    /**
     * Определить город по геолокации (Fused + Geocoder + geocoding API).
     * Предполагает, что ACCESS_COARSE_LOCATION уже выдан — это проверяет UI.
     */
    suspend fun detectCurrent(): AppResult<Location>
}
