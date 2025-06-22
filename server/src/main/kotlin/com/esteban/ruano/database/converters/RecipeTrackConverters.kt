package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.RecipeTrack
import com.esteban.ruano.models.nutrition.CreateRecipeTrackDTO
import com.esteban.ruano.models.nutrition.RecipeTrackDTO
import com.esteban.ruano.utils.formatDateTime

fun RecipeTrack.toDTO(): RecipeTrackDTO {
    return RecipeTrackDTO(
        id = this.id.toString(),
        consumedDateTime = formatDateTime(this.consumedDateTime),
        skipped = this.skipped,
        recipe = this.recipe.toDTO(),
    )
}
