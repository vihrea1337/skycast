package io.github.vihrea1337.skycast

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Configuration.Provider: WorkManager создаёт @HiltWorker-воркеры через Hilt.
 * Обязательная пара к этому — удаление дефолтного инициализатора WorkManager
 * из манифеста (tools:node="remove"), иначе он стартует раньше Hilt.
 */
@HiltAndroidApp
class SkyCastApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
