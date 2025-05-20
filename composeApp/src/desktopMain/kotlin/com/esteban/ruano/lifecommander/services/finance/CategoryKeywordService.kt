package com.esteban.ruano.lifecommander.services.finance

import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.CategoryKeyword
import com.esteban.ruano.lifecommander.utils.appHeaders
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl

class CategoryKeywordService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    suspend fun getCategoryKeywords(): List<CategoryKeyword> {
        return httpClient.get("$baseUrl/finance/category-keywords") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getCategoryKeyword(id: String): CategoryKeyword {
        return httpClient.get("$baseUrl/finance/category-keywords/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addKeyword(category: Category, keyword: String): CategoryKeyword {
        return httpClient.post("$baseUrl/finance/category-keywords") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(mapOf(
                "category" to category,
                "keyword" to keyword
            ))
        }.body()
    }

    suspend fun removeKeyword(category: Category, keyword: String) {
        httpClient.delete("$baseUrl/finance/category-keywords") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(mapOf(
                "category" to category,
                "keyword" to keyword
            ))
        }
    }

    suspend fun deleteMapping(mapping: CategoryKeyword) {
        httpClient.delete("$baseUrl/finance/category-keywords/${mapping.id}") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    suspend fun getKeywordsForCategory(category: Category): List<String> {
        return httpClient.get("$baseUrl/finance/category-keywords/category/${category.name}") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getCategoryForKeyword(keyword: String): Category? {
        return httpClient.get("$baseUrl/finance/category-keywords/keyword/$keyword") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }
} 