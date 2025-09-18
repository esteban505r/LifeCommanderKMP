package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.CategoryKeyword
import com.esteban.ruano.database.entities.CategoryKeywords
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.models.finance.CategoryKeywordResponseDTO
import com.esteban.ruano.models.finance.KeywordResponseDTO
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

class CategoryKeywordService : BaseService() {
    fun addKeyword(userId: Int, category: Category, keyword: String): UUID? {
        return transaction {
            CategoryKeywords.insertOperation(userId) {
                insert {
                    it[this.category] = category
                    it[this.keyword] = keyword
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getKeywordsByUser(userId: Int): List<CategoryKeywordResponseDTO> {
        return transaction {
            CategoryKeyword.find {
                (CategoryKeywords.user eq userId) and
                        (CategoryKeywords.status eq Status.ACTIVE)
            }.groupBy { it.category }
                .map { (category, keywords) ->
                    CategoryKeywordResponseDTO(
                        category = category.name,
                        keywords = keywords.map {
                            KeywordResponseDTO(
                                id = it.id.value.toString(),
                                keyword = it.keyword
                            )
                        }
                    )
                }
        }
    }

    fun getKeywordsByCategory(userId: Int, category: Category): List<CategoryKeywordResponseDTO> {
        return transaction {
            CategoryKeyword.find {
                (CategoryKeywords.user eq userId) and
                        (CategoryKeywords.category eq category) and
                        (CategoryKeywords.status eq Status.ACTIVE)
            }.map { it.toResponseDTO() }
        }
    }

    fun deleteKeyword(keywordId: UUID, userId: Int): Boolean {
        return transaction {
            val keyword = CategoryKeyword.findById(keywordId)
            if (keyword != null && keyword.user.id.value == userId) {
                keyword.status = Status.INACTIVE
                true
            } else {
                false
            }
        }
    }

    fun deleteAllKeywordsForCategory(category: Category, userId: Int): Boolean {
        return transaction {
            val keywords = CategoryKeyword.find { 
                (CategoryKeywords.user eq userId) and 
                (CategoryKeywords.category eq category) and
                (CategoryKeywords.status eq Status.ACTIVE)
            }
            keywords.forEach { it.status = Status.DELETED }
            keywords.toList().isNotEmpty()
        }
    }

    fun updateKeyword(keywordId: UUID, userId: Int, newKeyword: String): Boolean {
        return transaction {
            val keyword = CategoryKeyword.findById(keywordId)
            if (keyword != null && keyword.user.id.value == userId) {
                keyword.keyword = newKeyword
                true
            } else {
                false
            }
        }
    }
} 