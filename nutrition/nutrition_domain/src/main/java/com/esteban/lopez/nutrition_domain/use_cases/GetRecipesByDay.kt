package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class GetRecipesByDay (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        day: Int,
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
    ): Result<RecipesResponse> {
        return repository.getRecipesByDay(
            day = day,
            filter = filter,
            page = page,
            limit = limit,
        )
    }
}