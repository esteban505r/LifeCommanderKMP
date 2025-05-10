package com.esteban.ruano.tasks_data.remote

import com.esteban.ruano.tasks_data.remote.model.TaskResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {

    @GET("tasks")
    suspend fun getTasks(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): List<TaskResponse>

    @GET("tasks/byDateRange")
    suspend fun getTasksByDateRange(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): List<TaskResponse>

    @GET("tasks/noDueDate")
    suspend fun getTasksNoDueDate(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): List<TaskResponse>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): TaskResponse

    @POST("tasks")
    suspend fun addTask(
        @Body task: TaskResponse
    ): TaskResponse

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)

    @PATCH("tasks/{id}/complete")
    suspend fun completeTask(@Path("id") id: String, @Query("datetime") datetime: String)

    @PATCH("tasks/{id}/uncomplete")
    suspend fun unCompleteTask(@Path("id") id: String)

    @PATCH("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String,@Body task: TaskResponse)

}