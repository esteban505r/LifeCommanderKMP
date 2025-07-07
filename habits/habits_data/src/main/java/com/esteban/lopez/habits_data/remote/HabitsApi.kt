package com.esteban.ruano.habits_data.remote

import com.esteban.ruano.habits_data.remote.dto.HabitResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitsApi {

    @GET("habits")
    suspend fun getHabits(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("date") date: String?
    ): List<HabitResponse>

    @GET("habits/byDateRange")
    suspend fun getHabitsByDateRange(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): List<HabitResponse>

    @GET("habits/noDueDate")
    suspend fun getHabitsNoDueDate(
        @Query("filter") filter: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): List<HabitResponse>

    @GET("habits/{id}")
    suspend fun getHabit(@Path("id") id: String, @Query("date") date: String): HabitResponse

    @POST("habits")
    suspend fun addHabit(
        @Body habit: HabitResponse
    )

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String)

    @PATCH("habits/{id}/complete")
    suspend fun completeHabit(@Path("id") id: String, @Query("datetime") datetime: String,)

    @PATCH("habits/{id}/uncomplete")
    suspend fun unCompleteHabit(@Path("id") id: String,@Query("datetime") datetime: String)

    @PATCH("habits/{id}")
    suspend fun updateHabit(@Path("id") id: String,@Body habit: HabitResponse)

}