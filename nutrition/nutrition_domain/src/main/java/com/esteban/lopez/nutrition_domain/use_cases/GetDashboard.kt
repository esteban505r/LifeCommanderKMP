package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.NutritionRepository
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository

class GetDashboard (
    val repository: NutritionRepository
){
    suspend operator fun invoke(
        date:String
    ): Result<NutritionDashboardModel> {
        return repository.getDashboard(date)
    }
}