package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.service.TimerService
import kotlinx.serialization.Serializable
import java.util.UUID

fun Route.timerRouting(timerService: TimerService) {
    route("/timer") {
        route("/lists") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(timerService.getTimerLists(userId))
            }

            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val request = call.receive<CreateTimerListRequest>()
                val timerList = timerService.createTimerList(
                    userId = userId,
                    name = request.name,
                    loopTimers = request.loopTimers,
                    pomodoroGrouped = request.pomodoroGrouped
                )
                if (timerList != null) {
                    call.respond(HttpStatusCode.Created, timerList)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            route("/{id}") {
                get {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val timerList = timerService.getTimerList(id)
                    if (timerList != null) {
                        call.respond(timerList)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                patch {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val request = call.receive<CreateTimerListRequest>()
                    val timerList = timerService.updateTimerList(
                        listId = id,
                        name = request.name,
                        loopTimers = request.loopTimers,
                        pomodoroGrouped = request.pomodoroGrouped
                    )
                    if (timerList != null) {
                        call.respond(HttpStatusCode.OK, timerList)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                delete {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val deleted = timerService.deleteTimerList(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }

        route("/timers") {
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val request = call.receive<CreateTimerRequest>()
                val timer = timerService.createTimer(
                    listId = UUID.fromString(request.listId),
                    name = request.name,
                    duration = request.duration,
                    enabled = request.enabled,
                    countsAsPomodoro = request.countsAsPomodoro,
                    order = request.order
                )
                if (timer != null) {
                    call.respond(HttpStatusCode.Created, timer)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            route("/{id}") {
                patch {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val request = call.receive<CreateTimerRequest>()
                    val timer = timerService.updateTimer(
                        timerId = id,
                        name = request.name,
                        duration = request.duration,
                        enabled = request.enabled,
                        countsAsPomodoro = request.countsAsPomodoro,
                        order = request.order
                    )
                    if (timer != null) {
                        call.respond(HttpStatusCode.OK, timer)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                delete {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val deleted = timerService.deleteTimer(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }

        route("/settings") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val settings = timerService.getUserSettings(userId)
                if (settings != null) {
                    call.respond(settings)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val request = call.receive<UpdateUserSettingsRequest>()
                val settings = timerService.updateUserSettings(
                    userId = userId,
                    defaultTimerListId = request.defaultTimerListId?.let { UUID.fromString(it) },
                    dailyPomodoroGoal = request.dailyPomodoroGoal,
                    notificationsEnabled = request.notificationsEnabled
                )
                if (settings != null) {
                    call.respond(HttpStatusCode.OK, settings)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

@Serializable
data class CreateTimerListRequest(
    val name: String,
    val loopTimers: Boolean,
    val pomodoroGrouped: Boolean
)

@Serializable
data class CreateTimerRequest(
    val listId: String,
    val name: String,
    val duration: Int,
    val enabled: Boolean,
    val countsAsPomodoro: Boolean,
    val order: Int
)

@Serializable
data class UpdateUserSettingsRequest(
    val defaultTimerListId: String?,
    val dailyPomodoroGoal: Int,
    val notificationsEnabled: Boolean
) 