package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.repository.WorkoutRepository
import java.util.*

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
                call.respond(workoutRepository.getByDay(userId, day))
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
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val filter = call.request.queryParameters["filter"] ?: ""
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
                call.respond(workoutRepository.getExercises(userId, filter, limit, offset))
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
        }
    }
}