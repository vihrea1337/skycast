package io.github.vihrea1337.skycast.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.domain.location.DeleteLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.DetectMyLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.ObserveSavedLocationsUseCase
import io.github.vihrea1337.skycast.core.domain.location.ObserveSelectedLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.SaveLocationUseCase
import io.github.vihrea1337.skycast.core.domain.location.SearchCityUseCase
import io.github.vihrea1337.skycast.core.domain.location.SelectLocationUseCase
import io.github.vihrea1337.skycast.core.model.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCity: SearchCityUseCase,
    observeSavedLocations: ObserveSavedLocationsUseCase,
    observeSelectedLocation: ObserveSelectedLocationUseCase,
    private val selectLocation: SelectLocationUseCase,
    private val saveLocation: SaveLocationUseCase,
    private val deleteLocation: DeleteLocationUseCase,
    private val detectMyLocation: DetectMyLocationUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryText: StateFlow<String> = query.asStateFlow()

    /**
     * Эталонная цепочка поиска (docs/PLAN.md §5.11):
     * debounce — не дёргать API на каждую букву; distinctUntilChanged — не искать
     * то же самое повторно; transformLatest — новый ввод отменяет висящий запрос.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResultsState> = query
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .transformLatest { raw ->
            val text = raw.trim()
            if (text.length < MIN_QUERY_LENGTH) {
                emit(SearchResultsState.Idle)
                return@transformLatest
            }
            emit(SearchResultsState.Searching)
            emit(
                when (val result = searchCity(text)) {
                    is AppResult.Failure -> SearchResultsState.SearchError(result.error)
                    is AppResult.Success ->
                        if (result.data.isEmpty()) {
                            SearchResultsState.Empty
                        } else {
                            SearchResultsState.Results(result.data)
                        }
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SearchResultsState.Idle)

    val savedLocations: StateFlow<List<SavedLocationItem>> = combine(
        observeSavedLocations(),
        observeSelectedLocation(),
    ) { saved, selected ->
        saved.map { SavedLocationItem(location = it, isSelected = it.id == selected?.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _message = MutableStateFlow<SearchMessage?>(null)
    val message: StateFlow<SearchMessage?> = _message.asStateFlow()

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Результат геокодинга сохраняется только по явному клику (docs/PLAN.md §5.11). */
    fun onResultClick(location: Location) {
        viewModelScope.launch {
            saveLocation(location)
            _navigateBack.tryEmit(Unit)
        }
    }

    fun onSavedClick(id: Long) {
        viewModelScope.launch {
            selectLocation(id)
            _navigateBack.tryEmit(Unit)
        }
    }

    fun onDeleteSaved(id: Long) {
        viewModelScope.launch { deleteLocation(id) }
    }

    /** Вызывается, когда разрешение уже выдано — проверяет UI-слой. */
    fun onUseMyLocation() {
        viewModelScope.launch {
            _isDetecting.value = true
            when (detectMyLocation()) {
                is AppResult.Success -> _navigateBack.tryEmit(Unit)
                is AppResult.Failure -> _message.value = SearchMessage.DETECT_FAILED
            }
            _isDetecting.value = false
        }
    }

    fun onPermissionDenied() {
        _message.value = SearchMessage.PERMISSION_DENIED
    }

    fun onMessageShown() {
        _message.value = null
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
