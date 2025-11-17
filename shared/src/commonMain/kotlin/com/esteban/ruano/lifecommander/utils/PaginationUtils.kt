package com.esteban.ruano.lifecommander.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class PaginationState {
    object Loading : PaginationState()
    object Done : PaginationState()
    data class Error(val message: String) : PaginationState()
}

class PaginatedDataFetcher<T>(
    private val pageSize: Int,
    private val fetchData: suspend (page: Int, pageSize: Int) -> List<T> // Function to fetch data
) {
    private var currentPage = 0
    private var hasMore = true
    private val _state = MutableStateFlow<PaginationState>(PaginationState.Done)
    val state: StateFlow<PaginationState> get() = _state
    private val _data = MutableStateFlow<List<T>>(emptyList())
    val data: StateFlow<List<T>> get() = _data

    val loadMutex = Mutex()

    // Function to load more data

    fun reset() {
        currentPage = 0
        _data.value = emptyList()
        hasMore = true
    }

    suspend fun loadNextPage(reset: Boolean = false) {
        if(reset){
            currentPage = 0
            _data.value = emptyList()
            hasMore = true
        }
        if (loadMutex.isLocked) return
        if (!hasMore) return

        _state.value = PaginationState.Loading

        loadMutex.withLock {
            try {
                val result = fetchData(currentPage, pageSize)
                if (result.isNotEmpty()) {
                    _data.value += result
                    currentPage++
                } else {
                    hasMore = false
                }
                _state.value = PaginationState.Done
            } catch (e: Exception) {
                _state.value = PaginationState.Error("Failed to load data: ${e.message}")
            }
        }
    }
}
