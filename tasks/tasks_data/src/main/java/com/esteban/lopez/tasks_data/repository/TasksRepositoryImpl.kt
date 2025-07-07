package com.esteban.ruano.tasks_data.repository


import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_data.constants.DataConstants.DEFAULT_LIMIT
import com.esteban.ruano.core_data.constants.DataConstants.DEFAULT_PAGE
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_domain.repository.TasksRepository
import com.esteban.ruano.tasks_data.datasources.TaskDataSource
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class TasksRepositoryImpl (
    private val remoteDataSource: TaskDataSource,
    private val localDataSource: TaskDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
): BaseRepository(),TasksRepository {

    override suspend fun getTasks(
        filter: String?,
        page: Int?,
        limit: Int?
    ):Result<List<Task>> = doRequest(
        offlineModeEnabled = preferences.loadOfflineMode().first(),
        remoteFetch = {
            val result = remoteDataSource.getTasks(
                filter = filter ?: "",
                page = page?: DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
            )
            result
        },
        localFetch = {
            val result = localDataSource.getTasks(
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

    override suspend fun getTasksByDateRange(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String,
        endDate: String
    ): Result<List<Task>> = doRequest(
        offlineModeEnabled = preferences.loadOfflineMode().first(),
        remoteFetch = {
            val result = remoteDataSource.getTasksByDateRange(
                filter = filter?:"",
                page = page?:DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
                startDate = startDate,
                endDate = endDate
            )
            result
        },
        localFetch = {
            val result = localDataSource.getTasksByDateRange(
                filter = filter ?: "",
                page = page?: DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
                startDate = startDate,
                endDate = endDate
            )
            result
        },
        lastFetchTime = preferences.loadLastFetchTime().first(),
        isNetworkAvailable = networkHelper.isNetworkAvailable(),
        forceRefresh = false
    )

    override suspend fun getTasksByDateRangeWithSmartFiltering(
        filter: String?,
        page: Int?,
        limit: Int?,
        startDate: String,
        endDate: String,
        isTodayFilter: Boolean
    ): Result<List<Task>> = doRequest(
        offlineModeEnabled = preferences.loadOfflineMode().first(),
        remoteFetch = {
            val result = remoteDataSource.getTasksByDateRangeWithSmartFiltering(
                filter = filter?:"",
                page = page?:DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
                startDate = startDate,
                endDate = endDate,
                isTodayFilter = isTodayFilter
            )
            result
        },
        localFetch = {
            val result = localDataSource.getTasksByDateRangeWithSmartFiltering(
                filter = filter ?: "",
                page = page?: DEFAULT_PAGE,
                limit = limit?: DEFAULT_LIMIT,
                startDate = startDate,
                endDate = endDate,
                isTodayFilter = isTodayFilter
            )
            result
        },
        lastFetchTime = preferences.loadLastFetchTime().first(),
        isNetworkAvailable = networkHelper.isNetworkAvailable(),
        forceRefresh = false
    )

    override suspend fun getTasksNoDueDate(filter: String?, page: Int?, limit: Int?): Result<List<Task>> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getTasksNoDueDate(
                    filter = filter ?: "",
                    page = page?: DEFAULT_PAGE,
                    limit = limit?: DEFAULT_LIMIT,
                )
                result
            },
            localFetch = {
                val result = localDataSource.getTasksNoDueDate(
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

    override suspend fun getTask(taskId: String): Result<Task> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                val result = remoteDataSource.getTask(taskId)
                result
            },
            localFetch = {
                val result = localDataSource.getTask(taskId)
                result
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun addTask(task: Task): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.addTask(task)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.addTask(task)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun deleteTask(taskId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.deleteTask(taskId)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.deleteTask(taskId)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun completeTask(taskId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.completeTask(taskId, LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.completeTask(taskId, LocalDateTime.now().parseDateTime())
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )

    override suspend fun unCompleteTask(taskId: String): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.unCompleteTask(taskId)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.unCompleteTask(taskId)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )
    override suspend fun updateTask(id:String,task: Task): Result<Unit> =
        doRequest(
            offlineModeEnabled = preferences.loadOfflineMode().first(),
            remoteFetch = {
                remoteDataSource.updateTask(id,task)
                Result.success(Unit)
            },
            localFetch = {
                localDataSource.updateTask(id,task)
                Result.success(Unit)
            },
            lastFetchTime = preferences.loadLastFetchTime().first(),
            isNetworkAvailable = networkHelper.isNetworkAvailable(),
            forceRefresh = false
        )
}