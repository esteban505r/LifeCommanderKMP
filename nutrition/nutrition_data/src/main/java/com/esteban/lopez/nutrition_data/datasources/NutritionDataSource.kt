package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.nutrition_domain.model.Recipe

interface NutritionDataSource {
    suspend fun getDashboard(
        date:String
    ): NutritionDashboardModel
}
