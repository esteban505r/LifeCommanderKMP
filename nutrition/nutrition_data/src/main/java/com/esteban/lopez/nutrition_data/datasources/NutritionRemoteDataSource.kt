package com.esteban.ruano.nutrition_data.datasources

import com.esteban.ruano.nutrition_data.mappers.toDomainModel
import com.esteban.ruano.nutrition_data.remote.NutritionApi
import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel


class NutritionRemoteDataSource(
    private val api: NutritionApi
): NutritionDataSource {
    override suspend fun getDashboard(date: String): NutritionDashboardModel {
        return api.getDashboard(date).toDomainModel()
    }

}