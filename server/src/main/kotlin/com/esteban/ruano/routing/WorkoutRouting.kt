
package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.workout.CreateExerciseSetTrackDTO
import com.esteban.ruano.models.workout.CreateWorkoutTrackDTO
import com.esteban.ruano.models.workout.CreateExerciseTrackDTO
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.repository.WorkoutRepository
import java.util.UUID

fun Route.workoutRouting(workoutRepository: WorkoutRepository) {

    route("/workout") {
        route("/dashboard") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(workoutRepository.getWorkoutDashboard(userId))
            }
        }
        route("/byDay"){
            get("/{day}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val day = call.parameters["day"]?.toInt() ?: 0
                val dateTime = call.request.queryParameters["dateTime"]

                if (dateTime == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing dateTime parameter")
                    return@get
                }

                call.respond(workoutRepository.getByDay(userId, day,dateTime))
            }
        }
        route("/days") {
         get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(workoutRepository.getAll(userId))
            }

            get("/{workoutDayId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val workoutDayId = call.parameters["workoutDayId"]?.toInt()?:0
                call.respond(workoutRepository.getById(userId, workoutDayId))
            }

            patch("/{workoutDayId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val workoutDayId = call.parameters["workoutDayId"]?:"-1"
                val workoutDay = call.receive<UpdateWorkoutDayDTO>()
                workoutRepository.updateWorkoutDay(userId, workoutDayId, workoutDay)
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/exercises") {
            get("/{id}"){
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = call.parameters["id"]?:""
                call.respond(workoutRepository.getExercise(userId,id))
            }
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val filter = call.request.queryParameters["filter"] ?: ""
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
                call.respond(workoutRepository.getExercises(userId, filter, limit, offset))
            }
            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = call.parameters["id"]?:""
                val exercise = call.receive<ExerciseDTO>()
                val wasUpdated = workoutRepository.updateExercise(userId, UUID.fromString(id),exercise)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = call.parameters["id"]?:""
                val wasDeleted = workoutRepository.deleteExercise(userId, UUID.fromString(id))
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val exercise = call.receive<ExerciseDTO>()
                val wasCreated = workoutRepository.createExercise(userId, exercise)
                if (wasCreated!=null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            post("/bind") {
                try{
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val params = call.receive<Map<String, String>>()
                    val exerciseId = params["exerciseId"]
                    val workoutDayId = params["workoutDayId"]?.toIntOrNull()
                    if (exerciseId != null && workoutDayId != null) {
                        val success = workoutRepository.bindExerciseToDay(userId, exerciseId, workoutDayId)
                        if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                }
            }
            post("/unbind") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val params = call.receive<Map<String, String>>()
                val exerciseId = params["exerciseId"]
                val workoutDayId = params["workoutDayId"]?.toIntOrNull()
                if (exerciseId != null && workoutDayId != null) {
                    val success = workoutRepository.unbindExerciseFromDay(userId, exerciseId, workoutDayId)
                    if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        // Workout Tracking Endpoints
        route("/tracking") {
            post("/complete") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackRequest = call.receive<CreateWorkoutTrackDTO>()
                val success = workoutRepository.completeWorkout(userId, trackRequest.dayId, trackRequest.doneDateTime)
                if (success) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{trackId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackId = call.parameters["trackId"]!!
                val success = workoutRepository.unCompleteWorkout(userId, trackId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/week") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(workoutRepository.getWorkoutsCompletedPerDayThisWeek(userId))
            }

            get("/range") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val startDate = call.request.queryParameters["startDate"]!!
                val endDate = call.request.queryParameters["endDate"]!!
                call.respond(workoutRepository.getWorkoutTracksByDateRange(userId, startDate, endDate))
            }

            delete("/track/{trackId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackId = call.parameters["trackId"]!!
                val success = workoutRepository.deleteWorkoutTrack(userId, trackId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        // Exercise Tracking Endpoints
        route("/exercise-tracking") {
            route("/sets") {

                // Complete a set
                post("/complete") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val setRequest = call.receive<CreateExerciseSetTrackDTO>()
                    val success = workoutRepository.completeExerciseSet(
                        userId,
                        setRequest.exerciseId,
                        setRequest.workoutDayId,
                        setRequest.reps,
                        setRequest.doneDateTime
                    )
                    if (success) {
                        call.respond(HttpStatusCode.Created)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                // Uncomplete a set
                delete("/{setTrackId}") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val setTrackId = call.parameters["setTrackId"]!!
                    val success = workoutRepository.unCompleteExerciseSet(
                        userId,
                        setTrackId
                    )
                    if (success) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }


            post("/complete") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackRequest = call.receive<CreateExerciseTrackDTO>()
                val success = workoutRepository.completeExercise(userId, trackRequest.exerciseId, trackRequest.workoutDayId, trackRequest.doneDateTime)
                if (success) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{trackId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackId = call.parameters["trackId"]!!
                val success = workoutRepository.unCompleteExercise(userId, trackId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/range") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val startDate = call.request.queryParameters["startDate"]!!
                val endDate = call.request.queryParameters["endDate"]!!
                call.respond(workoutRepository.getExerciseTracksByDateRange(userId, startDate, endDate))
            }

            get("/completed/{workoutDayId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val workoutDayId = call.parameters["workoutDayId"]!!
                val dateTime = call.request.queryParameters["dateTime"] ?: throw BadRequestException("Missing dateTime parameter")
                call.respond(workoutRepository.getCompletedExercisesForDay(userId, workoutDayId,dateTime))
            }
        }
    }
}