package io.github.vihrea1337.skycast.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vihrea1337.skycast.core.common.result.AppResult
import io.github.vihrea1337.skycast.core.data.repository.LocationRepository
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
    private val locationRepository: LocationRepository,
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
                when (val result = locationRepository.search(text)) {
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
        locationRepository.observeSavedLocations(),
        locationRepository.observeSelected(),
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
            locationRepository.saveAndSelect(location)
            _navigateBack.tryEmit(Unit)
        }
    }

    fun onSavedClick(id: Long) {
        viewModelScope.launch {
            locationRepository.select(id)
            _navigateBack.tryEmit(Unit)
        }
    }

    fun onDeleteSaved(id: Long) {
        viewModelScope.launch { locationRepository.delete(id) }
    }

    /** Вызывается, когда разрешение уже выдано — проверяет UI-слой. */
    fun onUseMyLocation() {
        viewModelScope.launch {
            _isDetecting.value = true
            when (val result = locationRepository.detectCurrent()) {
                is AppResult.Success -> {
                    locationRepository.saveAndSelect(result.data)
                    _navigateBack.tryEmit(Unit)
                }
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
