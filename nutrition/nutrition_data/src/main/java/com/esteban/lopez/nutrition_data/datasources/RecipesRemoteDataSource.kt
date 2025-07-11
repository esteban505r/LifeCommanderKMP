package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_data.mappers.toDataModel
import com.esteban.ruano.nutrition_data.mappers.toDomainModel
import com.esteban.ruano.nutrition_data.remote.NutritionApi
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse


class RecipesRemoteDataSource(
    private val api: NutritionApi
): RecipesDataSource {
    override suspend fun getRecipes(filter: String, page: Int, limit: Int): RecipesResponse =
        api.getRecipes(filter, page, limit)

    override suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): RecipesResponse {
        return api.getRecipesByDay(day, filter, page, limit)
    }

    override suspend fun getAllRecipes(
        filter: String,
        page: Int,
        limit: Int
    ): RecipesResponse {
        return api.getAllRecipes(filter, page, limit)
    }

    override suspend fun getRecipe(recipeId: String): Recipe {
        return api.getRecipe(recipeId).toDomainModel()
    }

    override suspend fun addRecipe(recipe: Recipe) {
        api.addRecipe(recipe.toDataModel())
    }

    override suspend fun deleteRecipe(recipeId: String) {
        return api.deleteRecipe(recipeId)
    }

    override suspend fun updateRecipe(recipeId: String, recipe: Recipe) {
        return api.updateRecipe(recipeId, recipe.toDataModel())
    }
}