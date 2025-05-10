package com.esteban.ruano.habits_data.repository

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_data.constants.Constants.DEFAULT_LIMIT
import com.esteban.ruano.core_data.constants.Constants.DEFAULT_PAGE
import com.esteban.ruano.core_data.models.ErrorHandlingUtils
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.habits_data.datasources.HabitsDataSource
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_domain.repository.HabitsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class HabitsRepositoryImpl(
    val localDataSource: HabitsDataSource,
    val remoteDataSource: HabitsDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(), HabitsRepository {
    override suspend fun getHabits(
        filter: String?,
        page: Int?,
        limit: Int?,
        date: String
    ): Result<List<Habit>> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getHabits(
                    filter = filter ?: "",
                    page = page ?: DEFAULT_PAGE,
                    limit = limit ?: DEFAULT_LIMIT,
                    date = date
                )
                result
            },
            localFetch = {
                val result = localDataSource.getHabits(
                    filter = filter ?: "",
                    page = page ?: DEFAULT_PAGE,
                    limit = limit ?: DEFAULT_LIMIT,
                    date = date
                )
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun getHabit(habitId: String, date: String): Result<Habit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getHabit(habitId, date)
                result
            },
            localFetch = {
                val result = localDataSource.getHabit(habitId, date)
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun addHabit(habit: Habit): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.addHabit(habit)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.addHabit(habit)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun deleteHabit(habitId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.deleteHabit(habitId)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.deleteHabit(habitId)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun completeHabit(habitId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.completeHabit(habitId,LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.completeHabit(habitId,LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun unCompleteHabit(habitId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.unCompleteHabit(habitId,LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.unCompleteHabit(habitId,LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun updateHabit(id:String,habit: Habit): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.updateHabit(id,habit)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.updateHabit(id,habit)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun getHabitsByDateRange(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String?,
        endDate: String?
    ): Result<List<Habit>> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getHabitsByDateRange(
                    filter = filter ?: "",
                    page = page ?: 1,
                    limit = limit ?: 10,
                    startDate = startDate ?: "",
                    endDate = endDate ?: ""
                )
                result
            },
            localFetch = {
                val result = localDataSource.getHabitsByDateRange(
                    filter = filter ?: "",
                    page = page ?: 1,
                    limit = limit ?: 10,
                    startDate = startDate ?: "",
                    endDate = endDate ?: ""
                )
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )


}