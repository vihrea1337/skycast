package io.github.vihrea1337.skycast.core.data.widget

/**
 * Закрепление виджета из приложения (requestPinAppWidget, API 26+).
 * Реализация — в модуле :widget; UI показывает кнопку только при поддержке лаунчером.
 */
interface WidgetPinner {

    /** Поддерживает ли текущий лаунчер закрепление виджетов по запросу. */
    val isPinSupported: Boolean

    /** Показать системный диалог добавления виджета. true — диалог запрошен. */
    suspend fun requestPin(): Boolean
}
