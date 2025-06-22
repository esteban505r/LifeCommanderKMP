package com.esteban.ruano.lifecommander.services.meals

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.RecipeTrack
import com.esteban.ruano.lifecommander.models.CreateRecipeTrack
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl
import com.esteban.ruano.lifecommander.utils.appHeaders
import com.esteban.ruano.lifecommander.utils.buildParametersString

class RecipesService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    suspend fun getRecipes(
        limit: Int = 50, 
        offset: Int = 0,
        filters: RecipeFilters = RecipeFilters()
    ): RecipesResponse {
        val url = buildString {
            append("$baseUrl/nutrition/recipes")
            val params = mutableListOf<String>()
            params.add("limit=$limit")
            params.add("offset=$offset")
            
            // Add filter parameters
            val filterParams = filters.buildParametersString()
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
            
            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getRecipesByDay(
        day: Int, 
        limit: Int = 50, 
        offset: Int = 0,
        filters: RecipeFilters = RecipeFilters()
    ): RecipesResponse {
        val url = buildString {
            append("$baseUrl/nutrition/recipes/byDay/$day")
            val params = mutableListOf<String>()
            params.add("limit=$limit")
            params.add("offset=$offset")
            
            // Add filter parameters
            val filterParams = filters.buildParametersString()
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
            
            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getAllRecipes(
        limit: Int = 50, 
        offset: Int = 0,
        filters: RecipeFilters = RecipeFilters()
    ): RecipesResponse {
        val url = buildString {
            append("$baseUrl/nutrition/recipes/all")
            val params = mutableListOf<String>()
            params.add("limit=$limit")
            params.add("offset=$offset")
            
            // Add filter parameters
            val filterParams = filters.buildParametersString()
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
            
            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getRecipesWithFilters(
        limit: Int = 50,
        offset: Int = 0,
        filters: RecipeFilters = RecipeFilters()
    ): RecipesResponse {
        return getRecipes(limit, offset, filters)
    }

    suspend fun addRecipe(recipe: Recipe) {
        val response =  httpClient.post("$baseUrl/nutrition/recipes") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(recipe)
        }
        if (response.status != HttpStatusCode.Created) {
            throw Exception("Failed to add recipe: ${response.status}")
        }
    }

    suspend fun updateRecipe(recipe: Recipe) {
        val response = httpClient.patch("$baseUrl/nutrition/recipes/${recipe.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(recipe)
        }
        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            throw Exception("Failed to update recipe: ${response.status}")
        }
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

    suspend fun trackRecipeSkipped(recipeId: String, consumedDateTime: String): Boolean {
        val response = httpClient.post("$baseUrl/nutrition/tracking/consume") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateRecipeTrack(recipeId, consumedDateTime, skipped = true))
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun trackRecipeSkippedWithAlternative(
        recipeId: String, 
        consumedDateTime: String, 
        alternativeRecipeId: String?, 
        alternativeMealName: String?
    ): Boolean {
        val response = httpClient.post("$baseUrl/nutrition/tracking/consume") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateRecipeTrack(
                recipeId, 
                consumedDateTime, 
                skipped = true,
                alternativeRecipeId = alternativeRecipeId,
                alternativeMealName = alternativeMealName
            ))
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