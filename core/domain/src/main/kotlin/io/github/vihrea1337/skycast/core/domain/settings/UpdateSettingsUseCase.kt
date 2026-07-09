package io.github.vihrea1337.skycast.core.domain.settings

import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import io.github.vihrea1337.skycast.core.model.TemperatureUnit
import io.github.vihrea1337.skycast.core.model.ThemeMode
import io.github.vihrea1337.skycast.core.model.WindSpeedUnit
import javax.inject.Inject

/**
 * Композитный сценарий: единицы конвертирует сервер (docs/PLAN.md §5.4),
 * поэтому их смена = запись + refresh. Если сеть недоступна — данные остаются
 * в старых единицах со старыми подписями (units-at-sync, §5.12), рассинхрона нет.
 * Тема — просто запись, применяется мгновенно.
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
) {
    suspend fun temperatureUnit(unit: TemperatureUnit) {
        settingsRepository.setTemperatureUnit(unit)
        weatherRepository.refresh()
    }

    suspend fun windSpeedUnit(unit: WindSpeedUnit) {
        settingsRepository.setWindSpeedUnit(unit)
        weatherRepository.refresh()
    }

    suspend fun theme(mode: ThemeMode) {
        settingsRepository.setTheme(mode)
    }
}
