package com.esteban.ruano.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import com.esteban.ruano.lifecommander.models.finance.Keyword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CategoryKeywordState(
    val categoryKeywords: List<CategoryKeyword> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

