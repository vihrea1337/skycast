package io.github.vihrea1337.skycast.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY sortOrder")
    fun observeAll(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE isSelected = 1 LIMIT 1")
    fun observeSelected(): Flow<LocationEntity?>

    /** Атомарно поддерживает инвариант «выбран ровно один город». */
    @Transaction
    suspend fun select(id: Long) {
        clearSelection()
        setSelected(id)
    }

    /** Сохранить город (если новый — в конец списка) и сразу выбрать его. */
    @Transaction
    suspend fun saveAndSelect(location: LocationEntity) {
        upsert(location.copy(sortOrder = nextSortOrder()))
        select(location.id)
    }

    @Upsert
    suspend fun upsert(location: LocationEntity)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE locations SET isSelected = 0")
    suspend fun clearSelection()

    @Query("UPDATE locations SET isSelected = 1 WHERE id = :id")
    suspend fun setSelected(id: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM locations")
    suspend fun nextSortOrder(): Int
}
