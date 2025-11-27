package com.esteban.ruano.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.esteban.ruano.service.AuthService
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val authService: AuthService by inject()
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
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
                val logger = org.slf4j.LoggerFactory.getLogger("JWT-Auth")
                try {
                    val id = credential.payload.getClaim("id").asInt()
                    logger.info("JWT validation - User ID: $id")
                    id?.let(authService::findByID)?.also {
                        logger.info("JWT validation successful for user: ${it.id}")
                    } ?: run {
                        logger.warn("JWT validation failed - User ID not found in token")
                    }
                } catch (e: Exception) {
                    logger.error("JWT validation error: ${e.message}", e)
                    null
                }
            }
            challenge { defaultScheme, realm ->
                val logger = org.slf4j.LoggerFactory.getLogger("JWT-Auth")
                logger.warn("JWT authentication challenge - Scheme: $defaultScheme, Realm: $realm")
                logger.warn("Request URI: ${call.request.uri}")
                logger.warn("Request method: ${call.request.httpMethod}")
                logger.warn("Authorization header present: ${call.request.headers.contains("Authorization")}")
                call.respondText(
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    {
                        "Token is not valid or missing"
                    }
                )
            }
        }
    }
}
