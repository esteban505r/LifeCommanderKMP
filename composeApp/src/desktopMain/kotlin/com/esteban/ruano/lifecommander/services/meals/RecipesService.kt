package com.esteban.ruano.lifecommander.services.meals

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack
import com.esteban.ruano.lifecommander.models.CreateRecipeTrack
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

    // Recipe Tracking Methods
    suspend fun trackRecipeConsumption(recipeId: String, consumedDateTime: String): Boolean {
        val response = httpClient.post("$baseUrl/nutrition/tracking/consume") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateRecipeTrack(recipeId, consumedDateTime))
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun getRecipeTracksByDateRange(startDate: String, endDate: String): List<RecipeTrack> {
        return httpClient.get("$baseUrl/nutrition/tracking/range") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getRecipeTracksByRecipe(recipeId: String): List<RecipeTrack> {
        return httpClient.get("$baseUrl/nutrition/tracking/recipe/$recipeId") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun deleteRecipeTrack(trackId: String): Boolean {
        val response = httpClient.delete("$baseUrl/nutrition/tracking/track/$trackId") {
            appHeaders(tokenStorageImpl.getToken())
        }
        return response.status == HttpStatusCode.OK
    }
} 