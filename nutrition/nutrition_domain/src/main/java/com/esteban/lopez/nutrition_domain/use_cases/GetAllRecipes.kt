package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class GetAllRecipes(
    val repository: RecipesRepository
) {
    suspend operator fun invoke(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
    ): Result<List<Recipe>> {
        return repository.getAllRecipes(
            filter = filter,
            page = page,
            limit = limit,
        )
    }
} 