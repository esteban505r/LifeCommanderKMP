package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.pomodoros.CreatePomodoroDTO
import com.esteban.ruano.models.pomodoros.UpdatePomodoroDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.PomodoroRepository
import com.esteban.ruano.utils.Validator
import java.util.*

fun Route.pomodoroRouting(pomodoroRepository: PomodoroRepository) {

    route("/pomodoros") {
        get {
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            call.respond(pomodoroRepository.getAll(userId, limit, offset))
        }

        get("/byDateRange") {
            val startDate = call.request.queryParameters["startDate"]
            val endDate = call.request.queryParameters["endDate"]
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (startDate == null || endDate == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing start date or end date")
                return@get
            }

            try {
                val pomodoros = pomodoroRepository.getAllByDateRange(userId, startDate, endDate, limit, offset)
                call.respond(pomodoros)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date")
            }
        }

        post {
            val pomodoro = call.receive<CreatePomodoroDTO>()
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (!Validator.isValidDateTimeFormat(pomodoro.startDateTime) || !Validator.isValidDateTimeFormat(pomodoro.endDateTime)) {
                call.respond(HttpStatusCode.BadRequest, "Invalid datetime format")
                return@post
            }

            val wasCreated = pomodoroRepository.create(userId, pomodoro)
            if (wasCreated != null) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        route("/{id}") {
            get {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val pomodoro = pomodoroRepository.getByIdAndUserId(id, userId)
                if (pomodoro != null) {
                    call.respond(pomodoro)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val pomodoro = call.receive<UpdatePomodoroDTO>()

                if (pomodoro.startDateTime != null && !Validator.isValidDateTimeFormat(pomodoro.startDateTime)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid start datetime format")
                    return@patch
                }

                if (pomodoro.endDateTime != null && !Validator.isValidDateTimeFormat(pomodoro.endDateTime)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid end datetime format")
                    return@patch
                }

                val wasUpdated = pomodoroRepository.update(userId, id, pomodoro)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = pomodoroRepository.delete(userId, id)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
} 