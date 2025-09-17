package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.lifecommander.models.AlternativeNutrients
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse


interface RecipesDataSource {
    suspend fun getRecipes(
        filter:String,
        page:Int,
        limit:Int,
    ): RecipesResponse
    suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): RecipesResponse
    suspend fun getAllRecipes(
        filter: String,
        page: Int,
        limit: Int
    ): RecipesResponse
    suspend fun getRecipe(recipeId: String): Recipe
    suspend fun addRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipeId: String)
    suspend fun updateRecipe(recipeId: String,recipe:Recipe)

    suspend fun consumeRecipe(id: String,dateTime:String): Result<Unit>
    suspend fun skipRecipe(id: String,dateTime:String,
                           alternativeRecipeId: String? = null,
                           alternativeMealName: String? = null,
                           alternativeNutrients: AlternativeNutrients? = null): Result<Unit>

    suspend fun undoConsumedRecipe(id: String) : Result<Unit>
}
