package com.esteban.ruano.nutrition_data.repository


import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.core_data.constants.DataConstants.DEFAULT_LIMIT
import com.esteban.ruano.core_data.constants.DataConstants.DEFAULT_PAGE
import com.esteban.ruano.nutrition_data.datasources.RecipesDataSource
import com.esteban.ruano.nutrition_domain.model.Recipe
import com.esteban.ruano.nutrition_domain.repository.RecipesRepository
import kotlinx.coroutines.flow.first

class RecipesRepositoryImpl (
    private val remoteDataSource: RecipesDataSource,
    private val localDataSource: RecipesDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(), RecipesRepository {

    override suspend fun getRecipes(
        filter: String?,
        page: Int?,
        limit: Int?
    ):Result<List<Recipe>> = doRequest(
        offlineModeEnabled = preferences.loadOfflineMode().first(),
        remoteFetch = {
            val result = remoteDataSource.getRecipes(
                filter = filter ?: "",
                page = page?: DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
            )
            result
        },
        localFetch = {
            val result = localDataSource.getRecipes(
                filter = filter ?: "",
                page = page?: DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
            )
           result
        },
        lastFetchTime = preferences.loadLastFetchTime().first(),
        isNetworkAvailable = networkHelper.isNetworkAvailable(),
        forceRefresh = false
    )

    override suspend fun getRecipesByDay(
        day: Int,
        filter: String?,
        page: Int?,
        limit: Int?
    ): Result<List<Recipe>> {
        return doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getRecipesByDay(
                    day = day,
                    filter = filter ?: "",
                    page = page?: DEFAULT_PAGE,
                    limit = limit?: DEFAULT_LIMIT,
                )
                result
            },
            localFetch = {
                val result = localDataSource.getRecipesByDay(
                    day = day,
                    filter = filter ?: "",
                    page = page?: DEFAULT_PAGE,
                    limit = limit?: DEFAULT_LIMIT,
                )
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )
    }


    override suspend fun getRecipe(recipeId: String): Result<Recipe> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getRecipe(recipeId)
                result
            },
            localFetch = {
                val result = localDataSource.getRecipe(recipeId)
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun addRecipe(recipe: Recipe): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.addRecipe(recipe)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.addRecipe(recipe)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.deleteRecipe(recipeId)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.deleteRecipe(recipeId)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )


    override suspend fun updateRecipe(id:String, recipe: Recipe): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.updateRecipe(id,recipe)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.updateRecipe(id,recipe)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )
}