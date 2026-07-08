package io.github.vihrea1337.skycast.core.common.concurrency

import javax.inject.Qualifier

/**
 * Квалификатор для инжекта диспетчеров корутин.
 *
 * Прямое использование `Dispatchers.IO` в коде запрещено (detekt ForbiddenImport):
 * в тестах диспетчер должен подменяться на StandardTestDispatcher.
 * Hilt-провайдеры появятся вместе с первым потребителем (:core:data, этап 2).
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val type: DispatcherType)

enum class DispatcherType { IO, Default }
