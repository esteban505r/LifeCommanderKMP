package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class AddRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        recipe: Recipe
    ): Result<Unit> {
        return repository.addRecipe(
            recipe
        )
    }
}