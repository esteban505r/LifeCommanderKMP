package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class UpdateRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String,
        recipe: Recipe
    ): Result<Unit> {
        return repository.updateRecipe(
            id,
            recipe
        )
    }
}