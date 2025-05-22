package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.CategoryKeyword
import com.esteban.ruano.lifecommander.models.finance.Keyword
import com.esteban.ruano.models.finance.CategoryKeywordResponseDTO
import com.esteban.ruano.models.finance.KeywordResponseDTO

fun CategoryKeyword.toResponseDTO(
    keywords : List<KeywordResponseDTO> = listOf(KeywordResponseDTO(id = id.value.toString(), keyword = keyword))
) = CategoryKeywordResponseDTO(
    category = category.name,
    keywords = keywords
)

fun CategoryKeyword.toDomainModel() = com.esteban.ruano.lifecommander.models.finance.CategoryKeyword(
    id = id.value.toString(),
    category = category,
    keywords = listOf(Keyword(
        id = id.value.toString(),
        keyword = keyword
    ))
) 