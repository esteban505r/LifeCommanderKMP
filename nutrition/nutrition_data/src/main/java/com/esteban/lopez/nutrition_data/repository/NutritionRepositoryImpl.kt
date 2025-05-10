package com.esteban.ruano.nutrition_data.repository


import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.core_data.constants.Constants.DEFAULT_LIMIT
import com.esteban.ruano.core_data.constants.Constants.DEFAULT_PAGE
import com.esteban.ruano.nutrition_data.datasources.NutritionDataSource
import com.esteban.ruano.nutrition_data.datasources.RecipesDataSource
import com.esteban.ruano.nutrition_domain.model.NutritionDashboardModel
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.NutritionRepository
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository
import kotlinx.coroutines.flow.first

class NutritionRepositoryImpl (
    private val remoteDataSource: NutritionDataSource,
    private val localDataSource: NutritionDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(), NutritionRepository {
    override suspend fun getDashboard(date: String): Result<NutritionDashboardModel> {
        return doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.getDashboard(date)
            },
            localFetch = {
                localDataSource.getDashboard(date)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )
    }


}