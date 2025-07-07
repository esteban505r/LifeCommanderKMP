package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.SettingsRepository
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.service.NotificationService
import com.esteban.ruano.service.TimerService

fun Route.settingsRouting(settingsRepository: SettingsRepository, notificationService: NotificationService, timerService: TimerService) {
    route("/settings") {
        get {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val settings = settingsRepository.getUserSettings(userId)
            call.respond(settings)
        }

        put {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val settings = call.receive<UserSettings>()
            val updatedSettings = settingsRepository.updateUserSettings(userId, settings)
            call.respond(updatedSettings)
        }
        
        // Test notification endpoint
        post("/test-notification") {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            
            try {
                // Get user's FCM tokens
                val tokens = timerService.getDeviceTokensForUser(userId)
                
                if (tokens.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No FCM tokens found for user"))
                    return@post
                }
                
                // Send test notification
                notificationService.sendNotificationToTokens(
                    tokens = tokens,
                    title = "Test Notification",
                    body = "This is a test notification from LifeCommander!",
                    data = mapOf(
                        "type" to "TEST_NOTIFICATION",
                        "timestamp" to System.currentTimeMillis().toString()
                    )
                )
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Test notification sent successfully",
                    "tokensCount" to tokens.size.toString()
                ))
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Failed to send test notification: ${e.message}"
                ))
            }
        }
        
        // Test due tasks notification endpoint
        post("/test-due-tasks-notification") {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            
            try {
                // Get user's FCM tokens
                val tokens = timerService.getDeviceTokensForUser(userId)
                
                if (tokens.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No FCM tokens found for user"))
                    return@post
                }
                
                // Send due tasks test notification
                notificationService.sendNotificationToTokens(
                    tokens = tokens,
                    title = "Due Tasks Reminder",
                    body = "You have 3 tasks due today! Check your task list.",
                    data = mapOf(
                        "type" to "DUE_TASKS_NOTIFICATION",
                        "timestamp" to System.currentTimeMillis().toString(),
                        "dueTasksCount" to "3"
                    )
                )
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Due tasks test notification sent successfully",
                    "tokensCount" to tokens.size.toString()
                ))
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Failed to send due tasks test notification: ${e.message}"
                ))
            }
        }
        
        // Test due habits notification endpoint
        post("/test-due-habits-notification") {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            
            try {
                // Get user's FCM tokens
                val tokens = timerService.getDeviceTokensForUser(userId)
                
                if (tokens.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No FCM tokens found for user"))
                    return@post
                }
                
                // Send due habits test notification
                notificationService.sendNotificationToTokens(
                    tokens = tokens,
                    title = "Due Habits Reminder",
                    body = "You have 2 habits to complete today! Check your habit tracker.",
                    data = mapOf(
                        "type" to "DUE_HABITS_NOTIFICATION",
                        "timestamp" to System.currentTimeMillis().toString(),
                        "dueHabitsCount" to "2"
                    )
                )
                
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Due habits test notification sent successfully",
                    "tokensCount" to tokens.size.toString()
                ))
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Failed to send due habits test notification: ${e.message}"
                ))
            }
        }
    }
} 