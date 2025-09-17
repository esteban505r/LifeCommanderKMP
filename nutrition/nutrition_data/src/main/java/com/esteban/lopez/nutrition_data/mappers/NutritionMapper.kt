package com.esteban.ruano.nutrition_data.mappers

import com.esteban.ruano.nutrition_data.remote.model.NutritionDashboardResponse
import com.esteban.ruano.nutrition_data.remote.model.RecipeResponse
import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.lopez.nutrition_data.remote.model.IngredientResponse
import com.esteban.lopez.nutrition_data.remote.model.InstructionResponse
import com.esteban.ruano.lifecommander.models.Ingredient
import com.esteban.ruano.lifecommander.models.Instruction
import com.esteban.ruano.lifecommander.models.Recipes
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse

fun RecipeResponse.toDomainModel(): Recipe {
    return Recipe(
        id = id,
        name = name,
        note = note,
        protein = protein,
        calories = calories,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        sugar = sugar,
        image = image,
        days = days,
        mealTag = mealTag,
        ingredients = ingredients.map { it.toDomainModel() },
        instructions = instructions.map { it.toDomainModel() },
        consumed = consumed,
        consumedTrackId = consumedTrackId,
        consumedDateTime = consumedDateTime
    )
}


fun Recipe.toDataModel(): RecipeResponse {
    return RecipeResponse(
        id = id,
        name = name,
        note = note,
        protein = protein,
        calories = calories,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        sugar = sugar,
        image = image,
        days = days ?: emptyList(),
        mealTag = mealTag,
        consumed = consumed,
        consumedTrackId = consumedTrackId,
        consumedDateTime = consumedDateTime,
        ingredients = ingredients.map { it.toDataModel() },
        instructions = instructions.map { it.toDataModel() }
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

fun IngredientResponse.toDomainModel(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit
    )
}

fun InstructionResponse.toDomainModel(): Instruction {
    return Instruction(
        id = id,
        stepNumber = stepNumber,
        description = description
    )
}

fun Ingredient.toDataModel(): IngredientResponse {
    return IngredientResponse(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit
    )
}

fun Instruction.toDataModel(): InstructionResponse {
    return InstructionResponse(
        id = id,
        stepNumber = stepNumber,
        description = description
    )
}
