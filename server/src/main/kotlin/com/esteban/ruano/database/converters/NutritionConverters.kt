package com.esteban.ruano.database.converters

import formatDateTime
import kotlinx.datetime.*
import com.esteban.ruano.database.entities.Recipe
import com.esteban.ruano.database.entities.Task
import com.esteban.ruano.models.nutrition.RecipeDTO
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO
import toLocalDateTime


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


