package com.brewkery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.data.model.MenuItem
import com.brewkery.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val item: MenuItem) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val repository: MenuRepository = MenuRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val state: StateFlow<DetailUiState> = _state

    fun load(itemId: Int) {
        // Avoid refetching if we already hold this item (e.g. screen rotation).
        val current = _state.value
        if (current is DetailUiState.Success && current.item.id == itemId) return

        _state.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                val item = repository.loadItem(itemId)
                _state.value = DetailUiState.Success(item)
            } catch (e: Exception) {
                _state.value = DetailUiState.Error(
                    e.message ?: "Could not load item. Retry."
                )
            }
        }
    }
}
