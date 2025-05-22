package com.esteban.ruano.models.finance

import com.esteban.ruano.lifecommander.models.finance.Category
import kotlinx.serialization.Serializable
import java.util.*


@Serializable
data class CategoryKeywordResponseDTO(
    val category: String,
    val keywords: List<KeywordResponseDTO>,
)