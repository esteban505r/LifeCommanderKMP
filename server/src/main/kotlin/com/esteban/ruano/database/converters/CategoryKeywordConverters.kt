package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.CategoryKeyword
import com.esteban.ruano.models.finance.CategoryKeywordResponseDTO

fun CategoryKeyword.toResponseDTO(
    keywords : List<String> = listOf(keyword)
) = CategoryKeywordResponseDTO(
    category = category.name,
    keywords = keywords
)

fun CategoryKeyword.toDomainModel() = com.esteban.ruano.lifecommander.models.finance.CategoryKeyword(
    id = id.value.toString(),
    category = category,
    keywords = listOf(keyword)
) 