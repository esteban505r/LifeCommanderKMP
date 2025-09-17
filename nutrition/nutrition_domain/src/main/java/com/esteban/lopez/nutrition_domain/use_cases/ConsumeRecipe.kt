package com.esteban.ruano.nutrition_domain.use_cases


import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class ConsumeRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String,
        dateTime:String
    ): Result<Unit> {
        return repository.consumeRecipe(
            id,
            dateTime
        )
    }
}