package com.esteban.ruano.routing

import com.esteban.ruano.lifecommander.models.timers.UpdateUserSettingsRequest
import com.esteban.ruano.lifecommander.timer.CreateTimerListRequest
import com.esteban.ruano.lifecommander.timer.CreateTimerRequest
import com.esteban.ruano.lifecommander.timer.TimerWebSocketClientMessage
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import com.esteban.ruano.lifecommander.timer.UpdateTimerRequest
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.service.TimerNotifier
import com.esteban.ruano.service.TimerService
import com.esteban.ruano.service.webSocketJson
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
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
                sendNotificationOnComplete = request.sendNotificationOnComplete,
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
                    sendNotificationOnComplete = request.sendNotificationOnComplete,
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

        // Timer control endpoints
        route("/control") {
            route("/{listId}") {
                post("start") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val listId = call.parameters["listId"]?.let { UUID.fromString(it) }
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listId")
                    val timerId = call.request.queryParameters["timerId"]?.let { UUID.fromString(it) }
                    
                    try {
                        val timers = timerService.startTimer(userId, listId, timerId)
                        call.respond(timers)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Failed to start timer: ${e.message}")
                    }
                }
                
                post("pause") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val listId = call.parameters["listId"]?.let { UUID.fromString(it) }
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listId")
                    val timerId = call.request.queryParameters["timerId"]?.let { UUID.fromString(it) }
                    
                    try {
                        val timers = timerService.pauseTimer(userId, listId, timerId)
                        call.respond(timers)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Failed to pause timer: ${e.message}")
                    }
                }
                
                post("resume") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val listId = call.parameters["listId"]?.let { UUID.fromString(it) }
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listId")
                    
                    try {
                        val timers = timerService.resumeTimer(userId, listId)
                        call.respond(timers)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Failed to resume timer: ${e.message}")
                    }
                }
                
                post("stop") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val listId = call.parameters["listId"]?.let { UUID.fromString(it) }
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listId")
                    val timerId = call.request.queryParameters["timerId"]?.let { UUID.fromString(it) }
                    
                    try {
                        val timers = timerService.stopTimer(userId, listId, timerId)
                        call.respond(timers)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Failed to stop timer: ${e.message}")
                    }
                }
                
                post("restart") {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val listId = call.parameters["listId"]?.let { UUID.fromString(it) }
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listId")
                    val timerId = call.request.queryParameters["timerId"]?.let { UUID.fromString(it) }
                    
                    try {
                        val timers = timerService.restartTimer(userId, listId, timerId)
                        call.respond(timers)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Failed to restart timer: ${e.message}")
                    }
                }
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
                            val message = frame.readText()
                            try {
                                val messageObject = Json.decodeFromString<TimerWebSocketClientMessage>(
                                    message
                                )
                                when (messageObject) {
                                    is TimerWebSocketClientMessage.Ping -> {
                                        // Respond with pong containing server time
                                        val serverTime = System.currentTimeMillis()
                                        val pong = TimerWebSocketServerMessage.Pong(
                                            serverTime = serverTime,
                                            clientTime = messageObject.clientTime
                                        )
                                        send(Frame.Text(webSocketJson.encodeToString(
                                            TimerWebSocketServerMessage.serializer(),
                                            pong
                                        )))
                                    }
                                    is TimerWebSocketClientMessage.SubscribeTimers -> {
                                        // Subscription is implicit - user is already subscribed to their own timers
                                        // Could extend this for multi-user scenarios
                                        println("User $userId subscribed to timer updates")
                                    }
                                    is TimerWebSocketClientMessage.TimerUpdate -> {
                                        // Legacy support - timer updates from client
                                        // Note: Server is now authoritative, so this may be deprecated
                                        val timerUpdate = messageObject.timer
                                        val timerId = timerUpdate.id
                                        val listId = messageObject.listId
                                        
                                        timerService.updateTimer(
                                            timerId = UUID.fromString(timerId),
                                            userId = userId,
                                            name = timerUpdate.name,
                                            duration = timerUpdate.duration,
                                            enabled = timerUpdate.enabled,
                                            countsAsPomodoro = timerUpdate.countsAsPomodoro,
                                            order = timerUpdate.order,
                                            sendNotificationOnComplete = timerUpdate.sendNotificationOnComplete
                                        )
                                    }
                                }
                            } catch (e: kotlinx.serialization.SerializationException) {
                                println("Failed to parse WebSocket message: $message")
                            } catch (e: Exception) {
                                println("Error processing WebSocket message: ${e.message}")
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

