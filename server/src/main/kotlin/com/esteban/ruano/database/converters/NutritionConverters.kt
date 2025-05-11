package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.models.nutrition.RecipeDTO


fun Recipe.toDTO(): RecipeDTO {
    return RecipeDTO(
        id = this.id.toString(),
        name = this.name,
        protein = this.protein,
        image = this.image,
        day = this.day,
        note = this.note,
        mealTag = this.mealTag?.name
    )
}


