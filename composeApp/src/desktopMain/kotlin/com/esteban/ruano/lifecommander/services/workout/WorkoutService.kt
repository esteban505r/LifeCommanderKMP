package com.esteban.ruano.lifecommander.services.workout

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.WorkoutTrack
import com.esteban.ruano.lifecommander.models.CreateWorkoutTrack
import com.esteban.ruano.lifecommander.models.ExerciseTrack
import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.lifecommander.models.workout.day.UpdateWorkoutDay
import com.esteban.ruano.lifecommander.models.workout.day.WorkoutDay
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl
import com.esteban.ruano.lifecommander.utils.appHeaders

class WorkoutService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {

    suspend fun getAllWorkoutDays(): List<WorkoutDay> {
        return httpClient.get("$baseUrl/workout/days") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getWorkoutDayById(workoutDayId: Int): WorkoutDay {
        return httpClient.get("$baseUrl/workout/days/$workoutDayId") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun updateWorkoutDay(workoutDayId: String, workoutDay: UpdateWorkoutDay) {
        httpClient.patch("$baseUrl/workout/days/$workoutDayId") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(workoutDay)
        }
    }

    suspend fun getExercises(filter: String = "", limit: Int = 10, offset: Long = 0): List<Exercise> {
        return httpClient.get("$baseUrl/workout/exercises") {
            parameter("filter", filter)
            parameter("limit", limit)
            parameter("offset", offset)
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun createExercise(exercise: Exercise): Boolean {
        val response = httpClient.post("$baseUrl/workout/exercises") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(exercise)
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun getExercisesByDay(day: Int,dateTime:String): List<WorkoutDay> {
        return httpClient.get("$baseUrl/workout/byDay/$day") {
            parameter("dateTime", dateTime)
            appHeaders(tokenStorageImpl.getToken())
        }.body<List<WorkoutDay>>()
    }

    suspend fun addExercise(exercise: Exercise) {
        val response = httpClient.post("$baseUrl/workout/exercises") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(exercise)
        }
        if (response.status != HttpStatusCode.Created) {
            throw Exception("Failed to add exercise: ${response.status}")
        }
    }

    suspend fun updateExercise(exercise: Exercise) {
        val response = httpClient.patch("$baseUrl/workout/exercises/${exercise.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(exercise)
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to update exercise: ${response.status}")
        }
    }

    suspend fun deleteExercise(id: String) {
        httpClient.delete("$baseUrl/workout/exercises/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Workout Tracking Methods
    suspend fun completeWorkout(dayId: Int, doneDateTime: String): Boolean {
        val response = httpClient.post("$baseUrl/workout/tracking/complete") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateWorkoutTrack(dayId, doneDateTime))
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun unCompleteWorkout(trackId: String): Boolean {
        val response = httpClient.delete("$baseUrl/workout/tracking/$trackId") {
            appHeaders(tokenStorageImpl.getToken())
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun getWorkoutsCompletedPerDayThisWeek(): List<Int> {
        return httpClient.get("$baseUrl/workout/tracking/week") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getWorkoutTracksByDateRange(startDate: String, endDate: String): List<WorkoutTrack> {
        return httpClient.get("$baseUrl/workout/tracking/range") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun deleteWorkoutTrack(trackId: String): Boolean {
        val response = httpClient.delete("$baseUrl/workout/tracking/track/$trackId") {
            appHeaders(tokenStorageImpl.getToken())
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun bindExerciseToDay(exerciseId: String, workoutDayId: Int): Boolean {
        val response = httpClient.post("$baseUrl/workout/exercises/bind") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(mapOf("exerciseId" to exerciseId, "workoutDayId" to workoutDayId.toString()))
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun unbindExerciseFromDay(exerciseId: String, workoutDayId: Int): Boolean {
        val response = httpClient.delete("$baseUrl/workout/exercises/bind") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(mapOf("exerciseId" to exerciseId, "workoutDayId" to workoutDayId.toString()))
        }
        return response.status == HttpStatusCode.OK
    }

    // Exercise Tracking Methods
    suspend fun completeExercise(exerciseId: String, workoutDayId: String, doneDateTime: String): Boolean {
        val response = httpClient.post("$baseUrl/workout/exercise-tracking/complete") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateExerciseTrack(exerciseId, workoutDayId, doneDateTime))
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun unCompleteExercise(trackId: String): Boolean {
        val response = httpClient.delete("$baseUrl/workout/exercise-tracking/$trackId") {
            appHeaders(tokenStorageImpl.getToken())
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun getExerciseTracksByDateRange(startDate: String, endDate: String): List<ExerciseTrack> {
        return httpClient.get("$baseUrl/workout/exercise-tracking/range") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getCompletedExercisesForDay(workoutDayId: String, dateTime: String): List<String> {
        return httpClient.get("$baseUrl/workout/exercise-tracking/completed/$workoutDayId") {
            parameter("dateTime", dateTime)
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }
} 