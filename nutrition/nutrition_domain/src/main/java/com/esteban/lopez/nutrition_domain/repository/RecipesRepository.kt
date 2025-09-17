package com.esteban.ruano.nutrition_domain.repository

import com.esteban.ruano.lifecommander.models.AlternativeNutrients
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse


interface RecipesRepository {
    suspend fun getRecipes(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<RecipesResponse>
    suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<RecipesResponse>
    suspend fun getAllRecipes(
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<RecipesResponse>

    suspend fun getRecipe(recipeId: String): Result<Recipe>
    suspend fun addRecipe(recipe: Recipe): Result<Unit>
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun updateRecipe(id:String,recipe: Recipe): Result<Unit>

    suspend fun consumeRecipe(id: String, dateTime:String): Result<Unit>

    suspend fun  skipRecipe(id: String,dateTime:String,
                            alternativeRecipeId: String? = null,
                            alternativeMealName: String? = null,
                            alternativeNutrients: AlternativeNutrients? = null): Result<Unit>

    suspend fun undoConsumedRecipe(id: String): Result<Unit>


}