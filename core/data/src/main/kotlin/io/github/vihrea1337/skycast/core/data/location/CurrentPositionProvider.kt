package io.github.vihrea1337.skycast.core.data.location

/**
 * Платформенная геолокация за интерфейсом: репозиторий не зависит от Context
 * и play-services напрямую — detectCurrent() тестируется fake-реализацией.
 */
internal interface CurrentPositionProvider {

    /** null — позиция недоступна. Требует уже выданного COARSE-разрешения. */
    suspend fun currentPosition(): Position?

    /** Название населённого пункта по координатам (reverse-geocoding). */
    suspend fun localityName(position: Position): String?

    data class Position(val latitude: Double, val longitude: Double)
}
