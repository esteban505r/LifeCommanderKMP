package com.esteban.ruano.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CategoryKeywordState(
    val categoryKeywords: List<CategoryKeyword> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryKeywordViewModel {
    private val _state = MutableStateFlow(CategoryKeywordState())
    val state: StateFlow<CategoryKeywordState> = _state.asStateFlow()

    fun addKeyword(category: Category, keyword: String) {
        _state.update { currentState ->
            val existingMapping = currentState.categoryKeywords.find { it.category == category }
            if (existingMapping != null) {
                // Add keyword to existing mapping
                val updatedMapping = existingMapping.copy(
                    keywords = existingMapping.keywords + keyword
                )
                currentState.copy(
                    categoryKeywords = currentState.categoryKeywords.map {
                        if (it.category == category) updatedMapping else it
                    }
                )
            } else {
                // Create new mapping
                currentState.copy(
                    categoryKeywords = currentState.categoryKeywords + CategoryKeyword(
                        category = category,
                        keywords = listOf(keyword)
                    )
                )
            }
        }
    }

    fun removeKeyword(category: Category, keyword: String) {
        _state.update { currentState ->
            val existingMapping = currentState.categoryKeywords.find { it.category == category }
            if (existingMapping != null) {
                val updatedKeywords = existingMapping.keywords.filter { it != keyword }
                if (updatedKeywords.isEmpty()) {
                    // Remove the entire mapping if no keywords left
                    currentState.copy(
                        categoryKeywords = currentState.categoryKeywords.filter { it.category != category }
                    )
                } else {
                    // Update the mapping with remaining keywords
                    val updatedMapping = existingMapping.copy(keywords = updatedKeywords)
                    currentState.copy(
                        categoryKeywords = currentState.categoryKeywords.map {
                            if (it.category == category) updatedMapping else it
                        }
                    )
                }
            } else {
                currentState
            }
        }
    }

    fun deleteMapping(mapping: CategoryKeyword) {
        _state.update { currentState ->
            currentState.copy(
                categoryKeywords = currentState.categoryKeywords.filter { it.id != mapping.id }
            )
        }
    }

    fun loadCategoryKeywords(keywords: List<CategoryKeyword>) {
        _state.update { it.copy(categoryKeywords = keywords) }
    }

    fun setLoading(loading: Boolean) {
        _state.update { it.copy(isLoading = loading) }
    }

    fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }
} 