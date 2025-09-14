package com.esteban.ruano.workout_data.remote


import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.workout_data.remote.dto.ExerciseResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDashboardResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDayResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkoutApi {

    @GET("workout/days")
    suspend fun getWorkoutDays(

    ): List<WorkoutDayResponse>

    @GET("workout/days/{workoutId}")
    suspend fun getWorkoutDayById(
        @Path("workoutId") workoutId: String
    ): WorkoutDayResponse

    @GET("workout/days/{workoutDayId}/exercises")
    suspend fun getExercisesByWorkoutDay(
        @Path("workoutDayId") workoutDayId:String
    ): List<ExerciseResponse>

    @GET("workout/exercises")
    suspend fun getExercises(): List<ExerciseResponse>

    @POST("workout/exercises")
    suspend fun saveExercise(
        @Body exercise: ExerciseResponse
    )

    @POST("workout/days")
    suspend fun saveWorkoutDay(
        workoutDay: WorkoutDayResponse
    )

    @GET("workout/days/{number}")
    suspend fun getWorkoutDayByNumber(
        @Path("number") number: Int
    ): WorkoutDayResponse

    @POST("workout/days/{workoutDayId}/exercises/{exerciseId}")
    suspend fun linkExerciseWithWorkoutDay(
        @Path("workoutDayId") workoutDayId: String,
        @Path("exerciseId") exerciseId: String
    )

    @PATCH("workout/days/{workoutDayId}")
    suspend fun updateWorkoutDay(
        @Path("workoutDayId") workoutDayId: String,
        @Body workoutDay: WorkoutDayResponse
    )

    @GET("workout/dashboard")
    suspend fun getWorkoutDashboard(): WorkoutDashboardResponse

    @GET("workout/exercises/{exerciseId}")
    suspend fun getExerciseById(
        @Path("exerciseId") exerciseId: String
    ): ExerciseResponse

    @POST("workout/exercise-tracking/sets/complete")
    suspend fun addSet(@Body dto: CreateExerciseSetTrackDTO)
}