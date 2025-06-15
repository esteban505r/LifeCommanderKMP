package com.esteban.ruano.lifecommander.services.meals

import com.esteban.ruano.lifecommander.models.Recipe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl
import com.esteban.ruano.lifecommander.utils.appHeaders

class RecipesService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    suspend fun getRecipes(): List<Recipe> {
        return httpClient.get("$baseUrl/nutrition/recipes") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getRecipesByDay(day: Int): List<Recipe> {
        return httpClient.get("$baseUrl/nutrition/recipes/byDay/$day") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addRecipe(recipe: Recipe): Recipe {
        return httpClient.post("$baseUrl/nutrition/recipes") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(recipe)
        }.body()
    }

    suspend fun updateRecipe(recipe: Recipe): Recipe {
        return httpClient.patch("$baseUrl/nutrition/recipes/${recipe.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(recipe)
        }.body()
    }

    suspend fun deleteRecipe(id: String) {
        httpClient.delete("$baseUrl/nutrition/recipes/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }
} 