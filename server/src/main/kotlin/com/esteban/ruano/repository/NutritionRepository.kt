package com.esteban.ruano.repository

import com.esteban.ruano.models.nutrition.*
import com.esteban.ruano.service.NutritionService
import java.util.*

class NutritionRepository(private val nutritionService: NutritionService) {

    fun getAllRecipes(userId: Int, filter: String, limit: Int, offset: Long): List<RecipeDTO> {

        return nutritionService.fetchAllRecipes(
            userId,
            filter,
            limit,
            offset,
        )
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

    fun getRecipesByDay(userId: Int, day: Int): List<RecipeDTO> {
        return nutritionService.getRecipesByDay(userId, day)
    }

    fun getDashboard(userId: Int, date: String): NutritionDashboardDTO {
        return nutritionService.getDashboard(userId, date)
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