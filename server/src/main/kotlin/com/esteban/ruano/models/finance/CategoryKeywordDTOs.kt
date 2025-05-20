package com.esteban.ruano.models.finance

import com.esteban.ruano.lifecommander.models.finance.Category
import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryKeywordDTO(
    val category: Category,
    val keyword: String
)

@Serializable
data class UpdateCategoryKeywordDTO(
    val keyword: String
) 