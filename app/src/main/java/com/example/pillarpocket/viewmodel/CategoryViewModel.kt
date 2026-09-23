package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CategoryState {
    object Idle : CategoryState()
    object Loading : CategoryState()
    object Success : CategoryState()
    data class Error(val message: String) : CategoryState()
}

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val userId: Int
) : ViewModel() {

    private val _categoryState = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val categoryState: StateFlow<CategoryState> = _categoryState.asStateFlow()

    val categories: StateFlow<List<Category>> = repository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            _categoryState.value = CategoryState.Loading
            val result = repository.addCategory(userId, name, colorHex, iconName)
            _categoryState.value = result.fold(
                onSuccess = { CategoryState.Success },
                onFailure = { CategoryState.Error(it.message ?: "Failed to add category") }
            )
        }
    }

    fun updateCategory(category: Category, newName: String, newColorHex: String, newIconName: String) {
        viewModelScope.launch {
            _categoryState.value = CategoryState.Loading
            val result = repository.updateCategory(category, newName, newColorHex, newIconName)
            _categoryState.value = result.fold(
                onSuccess = { CategoryState.Success },
                onFailure = { CategoryState.Error(it.message ?: "Failed to update category") }
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _categoryState.value = CategoryState.Loading
            val result = repository.deleteCategory(category)
            _categoryState.value = result.fold(
                onSuccess = { CategoryState.Success },
                onFailure = { CategoryState.Error(it.message ?: "Failed to delete category") }
            )
        }
    }

    fun resetState() { _categoryState.value = CategoryState.Idle }
}