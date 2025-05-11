package com.esteban.ruano.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.esteban.ruano.service.AuthService

fun Application.configureSecurity() {
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    val repository = AuthService()
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                    JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                credential.payload.getClaim("id").asInt()?.let(repository::findByID)
            }
        }
    }
}
