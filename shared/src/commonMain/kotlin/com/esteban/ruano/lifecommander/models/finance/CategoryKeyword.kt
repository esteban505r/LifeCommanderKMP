package com.esteban.ruano.lifecommander.models.finance

data class CategoryKeyword(
    val id: String? = null,
    val category: Category,
    val keywords: List<Keyword> = emptyList(),
)