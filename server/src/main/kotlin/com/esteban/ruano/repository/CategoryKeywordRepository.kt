package com.esteban.ruano.repository

import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.models.finance.CategoryKeywordResponseDTO
import com.esteban.ruano.service.CategoryKeywordService
import java.util.*

class CategoryKeywordRepository(private val service: CategoryKeywordService) {
    fun addKeyword(userId: Int, category: Category, keyword: String): UUID? =
        service.addKeyword(userId, category, keyword)

    fun getKeywordsByUser(userId: Int): List<CategoryKeywordResponseDTO> =
        service.getKeywordsByUser(userId)

    fun getKeywordsByCategory(userId: Int, category: Category): List<CategoryKeywordResponseDTO> =
        service.getKeywordsByCategory(userId, category)

    fun deleteKeyword(userId: Int, keywordId: UUID): Boolean =
        service.deleteKeyword(keywordId, userId)

    fun deleteAllKeywordsForCategory(userId: Int, category: Category): Boolean =
        service.deleteAllKeywordsForCategory(category, userId)

    fun updateKeyword(userId: Int, keywordId: UUID, newKeyword: String): Boolean =
        service.updateKeyword(keywordId, userId, newKeyword)
} 