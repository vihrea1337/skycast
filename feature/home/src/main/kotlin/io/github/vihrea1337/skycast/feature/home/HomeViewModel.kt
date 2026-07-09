package io.github.vihrea1337.skycast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.common.result.AppError
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.domain.location.ObserveSelectedLocationUseCase
import io.github.vihrea1337.skycast.core.domain.weather.ObserveWeatherUseCase
import io.github.vihrea1337.skycast.core.domain.weather.RefreshWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeWeather: ObserveWeatherUseCase,
    observeSelectedLocation: ObserveSelectedLocationUseCase,
    private val refreshWeather: RefreshWeatherUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val transientError = MutableStateFlow<AppError?>(null)
    private val fatalError = MutableStateFlow<AppError?>(null)

    /**
     * База состояния — реактивные потоки из Room (SSOT): данные не хранятся
     * в ViewModel второй раз, флаги подмешиваются через combine (docs/PLAN.md §5.10).
     */
    val uiState: StateFlow<HomeUiState> = combine(
        observeSelectedLocation(),
        observeWeather(),
        isRefreshing,
        transientError,
        fatalError,
    ) { selected, weather, refreshing, transient, fatal ->
        when {
            selected == null -> HomeUiState.NoCity
            weather != null -> HomeUiState.Content(weather, refreshing, transient)
            refreshing -> HomeUiState.Loading
            fatal != null -> HomeUiState.Error(fatal)
            else -> HomeUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        // Переживает поворот экрана, но освобождает ресурсы в фоне
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = HomeUiState.Loading,
    )

    init {
        // Смена выбранного города (и первый запуск с городом) — повод обновиться
        viewModelScope.launch {
            observeSelectedLocation()
                .map { it?.id }
                .distinctUntilChanged()
                .collect { id -> if (id != null) refreshIfStale() }
        }
    }

    fun onRefresh() {
        viewModelScope.launch { refresh() }
    }

    fun onTransientErrorShown() {
        transientError.value = null
    }

    /** Политика устаревания: авто-refresh, если данным ≥ 30 минут или кэша нет. */
    private suspend fun refreshIfStale() {
        val cached = observeWeather().first()
        val isStale = cached == null ||
            Duration.between(cached.updatedAt, clock.instant()) >= STALE_AFTER
        if (isStale) refresh()
    }

    private suspend fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        when (val result = refreshWeather()) {
            is AppResult.Success -> fatalError.value = null
            is AppResult.Failure -> {
                val hasCache = observeWeather().first() != null
                if (hasCache) transientError.value = result.error else fatalError.value = result.error
            }
        }
        isRefreshing.value = false
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private val STALE_AFTER: Duration = Duration.ofMinutes(30)
    }
}
