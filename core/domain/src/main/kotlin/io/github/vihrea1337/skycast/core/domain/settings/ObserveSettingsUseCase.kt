package io.github.vihrea1337.skycast.core.domain.settings

import io.github.vihrea1337.skycast.core.data.repository.SettingsRepository
import io.github.vihrea1337.skycast.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<UserSettings> = settingsRepository.settings
}
