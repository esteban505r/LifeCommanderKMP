package com.esteban.ruano.routing

import com.esteban.ruano.lifecommander.models.timers.UpdateUserSettingsRequest
import com.esteban.ruano.lifecommander.timer.CreateTimerListRequest
import com.esteban.ruano.lifecommander.timer.CreateTimerRequest
import com.esteban.ruano.lifecommander.timer.TimerWebSocketClientMessage
import com.esteban.ruano.lifecommander.timer.UpdateTimerRequest
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.service.TimerNotifier
import com.esteban.ruano.service.TimerService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.*

fun Route.timerRouting(timerService: TimerService) {
    route("/timers") {

        post {
            val request = call.receive<CreateTimerRequest>()
            val timer = timerService.createTimer(
                listId = UUID.fromString(request.timerListId),
                name = request.name,
                duration = request.duration,
                enabled = request.enabled,
                countsAsPomodoro = request.countsAsPomodoro,
                order = request.order
            )
            call.respond(timer ?: HttpStatusCode.InternalServerError)
        }

        route("/{id}") {
            patch {
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<UpdateTimerRequest>()
                val timer = timerService.updateTimer(
                    timerId = id,
                    name = request.name,
                    duration = request.duration,
                    enabled = request.enabled,
                    countsAsPomodoro = request.countsAsPomodoro,
                    order = request.order,
                    userId = call.authentication.principal<LoggedUserDTO>()!!.id,
                ) ?: return@patch call.respond(HttpStatusCode.NotFound)
                call.respond(timer)
            }

            delete {
                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                timerService.deleteTimer(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        route("lists") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val timerLists = timerService.getTimerLists(userId)
                call.respond(timerLists)
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
                call.respond(timerList ?: HttpStatusCode.InternalServerError)
            }

            route("/{id}") {
                get {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val timerList = timerService.getTimerList(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(timerList)
                }

                patch {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    val request = call.receive<CreateTimerListRequest>()
                    val timerList = timerService.updateTimerList(
                        listId = id,
                        name = request.name,
                        loopTimers = request.loopTimers,
                        pomodoroGrouped = request.pomodoroGrouped
                    ) ?: return@patch call.respond(HttpStatusCode.NotFound)
                    call.respond(timerList)
                }

                delete {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    timerService.deleteTimerList(id)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        webSocket("/notifications") {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            TimerNotifier.registerSession(userId, this)
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            return@webSocket
                            val message = frame.readText()
                            val messageObject = Json.decodeFromString<TimerWebSocketClientMessage>(
                                message
                            )
                            println("Received message: $messageObject")
                            when (messageObject) {
                                is TimerWebSocketClientMessage.TimerUpdate -> {
                                    val timerUpdate = messageObject.timer
                                    val timerId = timerUpdate.id
                                    val listId = messageObject.listId
                                    val secondsRemaining = messageObject.remainingSeconds

                                    val timer = timerService.updateTimer(
                                        timerId = UUID.fromString(timerId),
                                        userId = userId,
                                        name = timerUpdate.name,
                                        duration = timerUpdate.duration,
                                        enabled = timerUpdate.enabled,
                                        countsAsPomodoro = timerUpdate.countsAsPomodoro,
                                        order = timerUpdate.order
                                    )
                                }
                            }

                        }

                        is Frame.Binary -> {
                            println("Received binary frame")
                        }

                        is Frame.Close -> {
                            println("WebSocket closed: ${frame.readReason()}")
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client closed the connection"))
                        }

                        else -> {
                            println("Unsupported frame type")
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.message}")
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "WebSocket error"))
            } finally {
                TimerNotifier.unregisterSession(userId, this)
            }
        }

        route("/settings") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val settings = timerService.getUserSettings(userId)
                call.respond(settings ?: HttpStatusCode.InternalServerError)
            }

            put {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val request = call.receive<UpdateUserSettingsRequest>()
                val settings = timerService.updateUserSettings(
                    userId = userId,
                    defaultTimerListId = request.defaultTimerListId?.let { UUID.fromString(it) },
                    dailyPomodoroGoal = request.dailyPomodoroGoal,
                    notificationsEnabled = request.notificationsEnabled
                )
                call.respond(settings ?: HttpStatusCode.InternalServerError)
            }
        }
    }

}

