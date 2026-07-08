// Единственное легальное место прямого обращения к Dispatchers:
// весь остальной код получает диспетчеры через @Dispatcher-квалификаторы
// и в тестах подменяет их на StandardTestDispatcher (docs/PLAN.md §5.2).
@file:Suppress("ForbiddenImport")

package io.github.vihrea1337.skycast.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.vihrea1337.skycast.core.common.concurrency.Dispatcher
import io.github.vihrea1337.skycast.core.common.concurrency.DispatcherType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
internal object DispatchersModule {

    @Provides
    @Dispatcher(DispatcherType.IO)
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(DispatcherType.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
