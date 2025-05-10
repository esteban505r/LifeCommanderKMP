package com.esteban.ruano.nutrition_domain.repository

import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel

interface NutritionRepository {
    suspend fun getDashboard(
        date: String
    ): Result<NutritionDashboardModel>
}