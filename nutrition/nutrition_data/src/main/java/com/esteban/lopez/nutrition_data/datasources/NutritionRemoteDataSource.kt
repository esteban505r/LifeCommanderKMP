package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_data.mappers.toDomainModel
import com.esteban.ruano.nutrition_data.remote.NutritionApi
import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel


class NutritionRemoteDataSource(
    private val api: NutritionApi
): NutritionDataSource {
    override suspend fun getDashboard(day: Int): NutritionDashboardModel {
        return api.getDashboard(day).toDomainModel()
    }

}