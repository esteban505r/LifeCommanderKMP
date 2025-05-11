package com.esteban.ruano.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object JWTUtils {
    fun makeJWT(id: Int): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("id", id)
            .withAudience("jwt-audience")
            .sign(Algorithm.HMAC256("secret"))
    }
}