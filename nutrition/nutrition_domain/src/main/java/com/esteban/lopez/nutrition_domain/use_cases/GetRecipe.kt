package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class GetRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String
    ): Result<Recipe> {
        return repository.getRecipe(
            id
        )
    }
}