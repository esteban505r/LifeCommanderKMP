package com.esteban.ruano.workout_data.remote


import com.esteban.ruano.lifecommander.models.BindExercise
import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.lifecommander.models.UnBindExercise
import com.esteban.ruano.workout_data.remote.dto.ExerciseResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDashboardResponse
import com.esteban.ruano.workout_data.remote.dto.WorkoutDayResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @PATCH("workout/exercises/{id}")
    suspend fun updateExercise(@Path("id") id:String,@Body exercise: Exercise)

    @DELETE("workout/exercises/{id}")
    suspend fun deleteExercise(@Path("id") id:String)

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

    @POST("workout/exercises/bind")
    suspend fun linkExerciseWithWorkoutDay(
        @Body dto: BindExercise
    )

    @POST("workout/exercises/unbind")
    suspend fun unLinkExerciseWithWorkoutDay(
        @Body dto: UnBindExercise
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
    ): Exercise

    @GET("workout/exercise-tracking/completed/{workoutDayId}")
    suspend fun getWorkoutDayStatus(
        @Path("workoutDayId") workoutDayId:String,
        @Query("dateTime") dateTime:String
    ): List<ExerciseDayStatus>

    @POST("workout/exercise-tracking/sets/complete")
    suspend fun addSet(@Body dto: CreateExerciseSetTrackDTO)

    @DELETE("workout/exercise-tracking/sets/{id}")
    suspend fun removeSet(@Path("id") id: String)

    @DELETE("workout/exercise-tracking/{trackId}")
    suspend fun undoExercise(@Path("trackId") trackId: String)

    @POST("workout/exercise-tracking/complete")
    suspend fun completeExercise(@Body() track: CreateExerciseTrack)



}