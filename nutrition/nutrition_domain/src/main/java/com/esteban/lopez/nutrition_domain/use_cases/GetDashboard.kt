package com.esteban.ruano.nutrition_domain.use_cases

import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.nutrition_domain.repository.NutritionRepository

class GetDashboard (
    val repository: NutritionRepository
){
    suspend operator fun invoke(
        date:String
    ): Result<NutritionDashboardModel> {
        return repository.getDashboard(date)
    }
}