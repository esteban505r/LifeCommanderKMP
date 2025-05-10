package com.esteban.ruano.nutrition_data.mappers

import com.esteban.ruano.nutrition_data.remote.model.NutritionDashboardResponse
import com.esteban.ruano.nutrition_data.remote.model.RecipeResponse
import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.nutrition_domain.model.Recipe

fun RecipeResponse.toDomainModel(): Recipe {
    return Recipe(
        id = id,
        name = name,
        note = note,
        protein = protein,
        image = image,
        day = day,
        mealTag = mealTag,
    )
}

fun Recipe.toDataModel(): RecipeResponse {
    return RecipeResponse(
        id = id,
        name = name,
        note = note,
        protein = protein,
        image = image,
        day = day,
        mealTag = mealTag
    )
}

fun NutritionDashboardResponse.toDomainModel(): NutritionDashboardModel {
    return NutritionDashboardModel(
        totalRecipes = totalRecipes,
        recipesForToday = recipesForToday.map { it.toDomainModel() }
    )
}

fun NutritionDashboardModel.toDataModel(): NutritionDashboardResponse {
    return NutritionDashboardResponse(
        totalRecipes = totalRecipes,
        recipesForToday = recipesForToday.map { it.toDataModel() }
    )
}
