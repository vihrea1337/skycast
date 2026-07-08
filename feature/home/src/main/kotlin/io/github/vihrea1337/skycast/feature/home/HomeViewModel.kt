package io.github.vihrea1337.skycast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.WeatherRepository
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadWeather()
    }

    fun onRetry() {
        loadWeather()
    }

    private fun loadWeather() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = weatherRepository.getWeather(DefaultLocation, UserSettings.Default)
            _uiState.value = when (result) {
                is AppResult.Success -> HomeUiState.Content(result.data)
                is AppResult.Failure -> HomeUiState.Error(result.error)
            }
        }
    }

    companion object {
        // Временно захардкожен: выбор города и геолокация появятся на этапе 4 (:feature:search)
        private val DefaultLocation = Location(
            id = 509820,
            name = "Петрозаводск",
            region = "Карелия",
            country = "Россия",
            latitude = 61.78491,
            longitude = 34.34691,
        )
    }
}
