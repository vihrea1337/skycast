package io.github.vihrea1337.skycast.core.data.widget

/**
 * Инверсия зависимости (docs/PLAN.md §5.13): data-слой уведомляет виджет
 * об обновлении данных, не зная про Glance. Реализация — в модуле :widget.
 */
interface WidgetUpdater {

    /** Перерисовать все экземпляры виджета из свежего кэша. */
    suspend fun updateAll()
}
