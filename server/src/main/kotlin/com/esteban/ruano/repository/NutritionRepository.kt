package com.esteban.ruano.repository

import com.esteban.ruano.models.nutrition.*
import com.esteban.ruano.service.NutritionService
import java.util.*

class NutritionRepository(private val nutritionService: NutritionService) {

    fun getAllRecipes(userId: Int, filter: String, limit: Int, offset: Long, sortBy: String = "name", sortOrder: String = "asc", mealTagFilter: String? = null, nutritionFilters: Map<String, Pair<Double?, Double?>> = emptyMap()): List<RecipeDTO> {
        return nutritionService.fetchAllRecipes(
            userId,
            filter,
            limit,
            offset,
            sortBy,
            sortOrder,
            mealTagFilter,
            nutritionFilters
        )
    }

    fun getRecipesWithFilters(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters = com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()
    ): RecipesResponseDTO {
        return nutritionService.getRecipesWithFilters(userId, limit, offset, filters)
    }

    fun getRecipesNotAssignedToDay(userId: Int, filter: String, limit: Int, offset: Long): List<RecipeDTO> {
        return nutritionService.getRecipesNotAssignedToDay(
            userId,
            filter,
            limit,
            offset,
        )
    }

    fun getRecipe(userId: Int, id: UUID): RecipeDTO? {
        return nutritionService.getRecipesByIdAndUserId(userId, id)
    }

    fun getRecipesByDay(userId: Int, day: Int, filter: String = "", limit: Int = 50, offset: Long = 0, sortBy: String = "name", sortOrder: String = "asc", mealTagFilter: String? = null, nutritionFilters: Map<String, Pair<Double?, Double?>> = emptyMap()): List<RecipeDTO> {
        return nutritionService.getRecipesByDay(userId, day, filter, limit, offset, sortBy, sortOrder, mealTagFilter, nutritionFilters)
    }

    fun getRecipesByDayWithFilters(
        userId: Int,
        day: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters = com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters()
    ): RecipesResponseDTO {
        return nutritionService.getRecipesByDayWithFilters(userId, day, limit, offset, filters)
    }

    fun getDashboard(userId: Int, day: Int): NutritionDashboardDTO {
        return nutritionService.getDashboard(userId, day)
    }

    fun create(userId: Int, task: CreateRecipeDTO): UUID? {
        return nutritionService.createRecipe(userId, task)
    }

    fun update(userId:Int,id: UUID, task: UpdateRecipeDTO): Boolean {
        return nutritionService.updateRecipe(userId,id, task)
    }

    fun delete(userId: Int,id: UUID): Boolean {
        return nutritionService.deleteRecipe(userId,id)
    }

    // Recipe Tracking Methods
    fun trackRecipeConsumption(userId: Int, recipeTrack: CreateRecipeTrackDTO): UUID? {
        return nutritionService.trackRecipeConsumption(userId, recipeTrack)
    }

    fun getRecipeTracksByDateRange(userId: Int, startDate: String, endDate: String): List<RecipeTrackDTO> {
        return nutritionService.getRecipeTracksByDateRange(userId, startDate, endDate)
    }

    fun getRecipeTracksByRecipe(userId: Int, recipeId: String): List<RecipeTrackDTO> {
        return nutritionService.getRecipeTracksByRecipe(userId, recipeId)
    }

    fun deleteRecipeTrack(userId: Int, trackId: String): Boolean {
        return nutritionService.deleteRecipeTrack(userId, trackId)
    }
}