package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel

interface NutritionDataSource {
    suspend fun getDashboard(
        day:Int
    ): NutritionDashboardModel
}
