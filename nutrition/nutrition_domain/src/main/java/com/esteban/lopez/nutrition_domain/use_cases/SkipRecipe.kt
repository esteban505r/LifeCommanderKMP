package com.esteban.ruano.nutrition_domain.use_cases


import com.esteban.ruano.lifecommander.models.AlternativeNutrients
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class SkipRecipe (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        id: String,
        dateTime:String,
        alternativeRecipeId: String? = null,
        alternativeMealName: String? = null,
        alternativeNutrients: AlternativeNutrients? = null
    ): Result<Unit> {
        return repository.skipRecipe(
            id,
            dateTime,
            alternativeRecipeId,
            alternativeMealName,
            alternativeNutrients
        )
    }
}