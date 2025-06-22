package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipesResponse
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class GetRecipes (
    val repository: RecipesRepository
){
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
    ): Result<RecipesResponse> {
        return repository.getRecipes(
            filter = filter,
            page = page,
            limit = limit,
        )
    }
}