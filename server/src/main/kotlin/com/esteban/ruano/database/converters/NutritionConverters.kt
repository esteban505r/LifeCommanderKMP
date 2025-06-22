package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Ingredient
import com.esteban.ruano.database.entities.Instruction
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.RecipeTrack
import com.esteban.ruano.models.nutrition.IngredientDTO
import com.esteban.ruano.models.nutrition.InstructionDTO
import com.esteban.ruano.models.nutrition.RecipeDTO
import com.esteban.ruano.models.nutrition.RecipeTrackDTO

fun Ingredient.toIngredientDTO(): IngredientDTO {
    return IngredientDTO(
        id = this.id.toString(),
        name = this.name,
        quantity = this.quantity,
        unit = this.unit
    )
}

fun Instruction.toInstructionDTO(): InstructionDTO {
    return InstructionDTO(
        id = this.id.toString(),
        stepNumber = this.stepNumber,
        description = this.description
    )
}

fun Recipe.toDTO(): RecipeDTO {
    return RecipeDTO(
        id = this.id.toString(),
        name = this.name,
        protein = this.protein,
        calories = this.calories,
        carbs = this.carbs,
        fat = this.fat,
        fiber = this.fiber,
        sugar = this.sugar,
        image = this.image,
        days = this.recipeDays.filter { it.status.name == "ACTIVE" }.map { it.day },
        note = this.note,
        mealTag = this.mealTag?.name,
        ingredients = this.ingredients.map { it.toIngredientDTO() },
        instructions = this.instructions.map { it.toInstructionDTO() }.sortedBy { it.stepNumber }
    )
}

fun RecipeTrack.toTrackDTO(): RecipeTrackDTO {
    val recipeDto = this.recipe.toDTO().copy(
        consumed = !this.skipped,
        consumedDateTime = this.consumedDateTime.toString()
    )
    return RecipeTrackDTO(
        id = this.id.value.toString(),
        recipe = recipeDto,
        skipped = this.skipped,
        consumedDateTime = this.consumedDateTime.toString()
    )
}


