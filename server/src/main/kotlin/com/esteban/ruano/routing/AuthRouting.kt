package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoginUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO
import com.esteban.ruano.service.AuthService
import com.esteban.ruano.utils.JWTUtils
import com.esteban.ruano.utils.SecurityUtils.hashPassword
import com.esteban.ruano.service.TimerService
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.lifecommander.models.ForgotPasswordRequest
import com.esteban.ruano.lifecommander.models.ResetPasswordRequest
import com.esteban.ruano.lifecommander.models.VerifyTokenRequest
import com.esteban.ruano.service.PasswordResetService
import com.esteban.ruano.utils.isLikelyEmail
import com.esteban.ruano.utils.isStrongPassword
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

fun Route.authRouting(
    authService: AuthService,
    passwordResetService: PasswordResetService, timerService: TimerService
) {
    route("/auth") {
        route("/login") {
            post {
                val credentials = call.receive<LoginUserDTO>()
                val userLogged = authService.login(credentials.email, credentials.password)
                if (userLogged != null) {
                    // Register FCM token if provided
                    if (!credentials.fcmToken.isNullOrBlank()) {
                        timerService.registerDeviceToken(
                            userLogged.id,
                            credentials.fcmToken,
                            "android"
                        )
                    }
                    // Update timezone if provided
                    if (!credentials.timezone.isNullOrBlank()) {
                        transaction {
                            Users.update({ Users.id eq userLogged.id }) {
                                it[timeZone] = credentials.timezone
                            }
                        }
                    }
                    call.respond(mapOf("token" to JWTUtils.makeJWT(userLogged.id)))
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
        route("/register") {
            post {
                val user = call.receive<RegisterUserDTO>().hashPassword()
                val wasCreated = authService.register(user)
                if (wasCreated) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
        route("/{id}") {
            get {
                call.respondText("Get user with id ${call.parameters["id"]}")
            }
            put {
                call.respondText("Update user with id ${call.parameters["id"]}")
            }
            delete {
                call.respondText("Delete user with id ${call.parameters["id"]}")
            }
        }

        rateLimit(RateLimitName("forgot")) {
            post("/forgot-password") {
                val payload = runCatching { call.receive<ForgotPasswordRequest>() }.getOrNull()
                if (payload?.email.isNullOrBlank() || !isLikelyEmail(payload!!.email)) {
                    call.respond(HttpStatusCode.OK)
                    return@post
                }
                val ip = call.request.origin.remoteHost
                passwordResetService.requestReset(payload.email.trim(), ip)
                call.respond(HttpStatusCode.OK)
            }
        }

        rateLimit(RateLimitName("verify")) {
            post("/reset/verify") {
                val payload = runCatching { call.receive<VerifyTokenRequest>() }.getOrNull()
                val token = payload?.token?.trim().orEmpty()
                if (token.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                    return@post
                }

                val valid = passwordResetService.isTokenValid(token)
                if (valid) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                }
            }
        }

        // POST /auth/reset { token, newPassword }
        rateLimit(RateLimitName("reset")) {
            post("/reset-password") {
                val payload = runCatching { call.receive<ResetPasswordRequest>() }.getOrNull()
                if (payload == null || !isStrongPassword(payload.newPassword) || payload.token.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                    return@post
                }

                val ok = passwordResetService.resetPassword(
                    rawToken = payload.token.trim(),
                    newPassword = payload.newPassword
                )

                if (ok) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    // Keep it generic: token invalid/expired/used → 400
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                }
            }
        }


    }
}