package com.esteban.ruano.nutrition_domain.use_cases


import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class UndoConsumedRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String
    ): Result<Unit> {
        return repository.undoConsumedRecipe(
            id
        )
    }
}