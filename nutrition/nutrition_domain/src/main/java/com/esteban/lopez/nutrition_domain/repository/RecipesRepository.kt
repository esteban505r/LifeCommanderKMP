package com.esteban.ruano.nutrition_domain.repository

import com.esteban.ruano.nutrition_domain.model.Recipe

interface RecipesRepository {
    suspend fun getRecipes(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Recipe>>
    suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Recipe>>
    suspend fun getAllRecipes(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Recipe>>

    suspend fun getRecipe(recipeId: String): Result<Recipe>
    suspend fun addRecipe(recipe: Recipe): Result<Unit>
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun updateRecipe(id:String,recipe: Recipe): Result<Unit>
}