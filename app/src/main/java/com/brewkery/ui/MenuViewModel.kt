package com.brewkery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewkery.data.model.BrewkeryMenuResponse
import com.brewkery.data.model.MenuItem
import com.brewkery.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Success(
        val data: BrewkeryMenuResponse,
        val selectedCategoryId: String = "ALL"
    ) : MenuUiState
    data class Error(val message: String) : MenuUiState
}

class MenuViewModel(
    private val repository: MenuRepository = MenuRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val state: StateFlow<MenuUiState> = _state

    init {
        load()
    }

    fun load() {
        _state.value = MenuUiState.Loading
        viewModelScope.launch {
            try {
                val data = repository.loadMenu()
                _state.value = MenuUiState.Success(data)
            } catch (e: Exception) {
                _state.value = MenuUiState.Error(
                    e.message ?: "Network error. Check connection and retry."
                )
            }
        }
    }

    fun selectCategory(categoryId: String) {
        val current = _state.value
        if (current is MenuUiState.Success) {
            _state.value = current.copy(selectedCategoryId = categoryId)
        }
    }

    fun visibleItems(): List<MenuItem> {
        val current = _state.value
        if (current !is MenuUiState.Success) return emptyList()
        return if (current.selectedCategoryId == "ALL") {
            current.data.items
        } else {
            current.data.items.filter { it.categoryId == current.selectedCategoryId }
        }
    }
}
