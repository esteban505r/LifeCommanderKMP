package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_domain.model.Recipe

interface RecipesDataSource {
    suspend fun getRecipes(
        filter:String,
        page:Int,
        limit:Int,
    ): List<Recipe>
    suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): List<Recipe>
    suspend fun getAllRecipes(
        filter: String,
        page: Int,
        limit: Int
    ): List<Recipe>
    suspend fun getRecipe(recipeId: String): Recipe
    suspend fun addRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipeId: String)
    suspend fun updateRecipe(recipeId: String,recipe:Recipe)
}
