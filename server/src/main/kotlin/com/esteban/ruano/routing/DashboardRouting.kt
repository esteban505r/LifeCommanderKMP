package com.esteban.ruano.routing

import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.service.DashboardService
import com.esteban.ruano.utils.Validator
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRouting(
    dashboardService: DashboardService
) {
    route("/dashboard") {
        authenticate {
            get {
                try {
                    val userId = call.authentication.principal<LoggedUserDTO>()?.id

                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                        return@get
                    }

                    val dateTime = call.request.queryParameters["dateTime"]
                    if (dateTime == null) {
                        call.respond(HttpStatusCode.BadRequest, "Date parameter is required")
                        return@get
                    }

                    val isValid = Validator.isValidDateTimeFormat(dateTime)
                    if (!isValid) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid date time format")
                        return@get
                    }

                    val dashboardData = dashboardService.getDashboardData(userId, dateTime)
                    call.respond(dashboardData)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                }
            }
        }
    }
} 