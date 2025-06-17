package com.esteban.ruano.lifecommander.services.workout

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.Workout
import com.esteban.ruano.lifecommander.models.WorkoutTrack
import com.esteban.ruano.lifecommander.models.CreateWorkoutTrack
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

    suspend fun getWorkoutsByDay(day: Int): List<Workout> {
        return httpClient.get("$baseUrl/workout/byDay/$day") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addWorkout(workout: Workout): Workout {
        return httpClient.post("$baseUrl/workout/workouts") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(workout)
        }.body()
    }

    suspend fun updateWorkout(workout: Workout): Workout {
        return httpClient.patch("$baseUrl/workout/workouts/${workout.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(workout)
        }.body()
    }

    suspend fun deleteWorkout(id: String) {
        httpClient.delete("$baseUrl/workout/workouts/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    suspend fun getWorkoutDashboard(): Any {
        return httpClient.get("$baseUrl/workout/dashboard") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

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

    suspend fun getExercisesByDay(day: Int): List<Exercise> {
        return httpClient.get("$baseUrl/workout/byDay/$day") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addExercise(exercise: Exercise): Exercise {
        return httpClient.post("$baseUrl/workout/exercises") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(exercise)
        }.body()
    }

    suspend fun updateExercise(exercise: Exercise): Exercise {
        return httpClient.patch("$baseUrl/workout/exercises/${exercise.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(exercise)
        }.body()
    }

    suspend fun deleteExercise(id: String) {
        httpClient.delete("$baseUrl/workout/exercises/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Workout Tracking Methods
    suspend fun completeWorkout(workoutDayId: String, doneDateTime: String): Boolean {
        val response = httpClient.post("$baseUrl/workout/tracking/complete") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(CreateWorkoutTrack(workoutDayId, doneDateTime))
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
} 