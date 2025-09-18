package com.esteban.ruano.routing

import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.lifecommander.models.ForgotPasswordRequest
import com.esteban.ruano.lifecommander.models.ResetWithSessionRequest
import com.esteban.ruano.lifecommander.models.VerifyPinRequest
import com.esteban.ruano.models.users.LoginUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO
import com.esteban.ruano.service.AuthService
import com.esteban.ruano.service.PasswordResetService
import com.esteban.ruano.service.TimerService
import com.esteban.ruano.utils.JWTUtils
import com.esteban.ruano.utils.SecurityUtils.hashPassword
import com.esteban.ruano.utils.isLikelyEmail
import com.esteban.ruano.utils.isStrongPassword
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.eq
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
                val wasCreated = if(user.email.isLikelyEmail()){
                    authService.register(user)
                }
                else{
                    throw BadRequestException("Invalid email")
                }

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

        // Step 1: request PIN
        rateLimit(RateLimitName("forgot")) {
            post("/forgot-password") {
                val payload = runCatching { call.receive<ForgotPasswordRequest>() }.getOrNull()
                if (payload?.email.isNullOrBlank()) {
                    call.respond(HttpStatusCode.OK); return@post
                }
                val ip = call.request.origin.remoteHost
                passwordResetService.requestReset(payload!!.email, ip)
                call.respond(HttpStatusCode.OK)
            }
        }

        // Step 2: verify PIN -> returns reset session token
        rateLimit(RateLimitName("verify")) {
            post("/reset-password/verify") {
                val payload = runCatching { call.receive<VerifyPinRequest>() }.getOrNull()
                if (payload == null || payload.email.isBlank() || payload.pin.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request")); return@post
                }
                val token = passwordResetService.verifyPin(payload.email, payload.pin)
                if (token != null) {
                    call.respond(HttpStatusCode.OK, mapOf("reset_token" to token))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                }
            }
        }

        // Step 3: consume reset session token to set password
        rateLimit(RateLimitName("reset")) {
            post("/reset-password") {
                val payload = runCatching { call.receive<ResetWithSessionRequest>() }.getOrNull()
                if (payload == null || payload.resetToken.isBlank() || !isStrongPassword(payload.newPassword)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request")); return@post
                }
                val ok = passwordResetService.resetPassword(payload.resetToken, payload.newPassword)
                if (ok) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
            }
        }


    }
}