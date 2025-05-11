package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoginUserDTO
import com.esteban.ruano.models.users.RegisterUserDTO
import com.esteban.ruano.service.AuthService
import com.esteban.ruano.utils.JWTUtils
import com.esteban.ruano.utils.SecurityUtils.hashPassword

fun Route.authRouting(authService: AuthService) {
    route("/auth") {
        route("/login") {
            post {
                val credentials = call.receive<LoginUserDTO>()
                val userLogged = authService.login(credentials.email, credentials.password)
                if (userLogged != null) {
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
    }
}