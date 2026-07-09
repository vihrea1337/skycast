package io.github.vihrea1337.skycast.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.domain.weather.RefreshWeatherUseCase

/**
 * Фоновое обновление данных для виджета.
 * Требует HiltWorkerFactory в Application (см. SkyCastApplication).
 */
@HiltWorker
class WeatherWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshWeather: RefreshWeatherUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = refreshWeather()
        // Перерисовываем в любом случае: хотя бы обновится «N мин назад»
        WeatherWidget().updateAll(applicationContext)
        return when (result) {
            is AppResult.Success -> Result.success()
            is AppResult.Failure ->
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
