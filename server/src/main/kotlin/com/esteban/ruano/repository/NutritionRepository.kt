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

}