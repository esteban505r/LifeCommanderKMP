package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import com.esteban.ruano.lifecommander.services.finance.CategoryKeywordService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryKeywordMapperState(
    val categoryKeywords: List<CategoryKeyword> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryKeywordMapperViewModel(
    private val service: CategoryKeywordService
) : ViewModel() {
    private val _state = MutableStateFlow(CategoryKeywordMapperState())
    val state: StateFlow<CategoryKeywordMapperState> = _state.asStateFlow()


    fun loadCategoryKeywords() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val categoryKeywords = service.getCategoryKeywords()
                _state.update { it.copy(categoryKeywords = categoryKeywords, isLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load category keywords"
                    )
                }
            }
        }
    }

    fun addKeyword(category: Category, keyword: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                service.addKeyword(category, keyword)
                loadCategoryKeywords()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add keyword"
                    )
                }
            }
        }
    }

    fun removeKeyword(category: Category, keyword: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                service.removeKeyword(category, keyword)
                loadCategoryKeywords()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to remove keyword"
                    )
                }
            }
        }
    }

    fun deleteMapping(mapping: CategoryKeyword) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                service.deleteMapping(mapping)
                loadCategoryKeywords()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete mapping"
                    )
                }
            }
        }
    }
} 