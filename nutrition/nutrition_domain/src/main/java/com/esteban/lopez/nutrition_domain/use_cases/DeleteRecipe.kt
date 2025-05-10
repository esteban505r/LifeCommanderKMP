package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class DeleteRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String
    ): Result<Unit> {
        return repository.deleteRecipe(
            id
        )
    }
}