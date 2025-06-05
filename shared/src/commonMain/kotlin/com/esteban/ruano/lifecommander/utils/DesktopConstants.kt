package com.esteban.ruano.lifecommander.utils

import io.ktor.client.request.*
import io.ktor.http.*

const val BASE_URL_DEV = "http://localhost:8080/api/v1"
const val BASE_URL_PROD = "http://ec2-3-91-21-254.compute-1.amazonaws.com:8080/api/v1"
//const val BASE_URL_DEV = "http://ec2-3-91-21-254.compute-1.amazonaws.com:8080/api/v1" //FOR DEBUGGING PROD
const val SOCKETS_HOST_DEV = "localhost"
const val SOCKETS_HOST_PROD = "ec2-3-91-21-254.compute-1.amazonaws.com"
const val PROD_VARIANT = "prod"
const val DEV_VARIANT = "dev"
const val APP_NAME_BASE = "Life Commander"
const val VERSION = "0.0.0.1"


const val VARIANT = DEV_VARIANT
//const val VARIANT = PROD_VARIANT



val APP_NAME: String
    get() {
        return when (VARIANT) {
            PROD_VARIANT -> "$APP_NAME_BASE $VERSION"
            DEV_VARIANT -> "$APP_NAME_BASE $VERSION (DEV)"
            else -> throw IllegalArgumentException("Unknown variant: $VARIANT")
        }
    }

val BASE_URL: String
    get() {
        return when (VARIANT) {
            PROD_VARIANT -> BASE_URL_PROD
            DEV_VARIANT -> BASE_URL_DEV
            else -> throw IllegalArgumentException("Unknown variant: $VARIANT")
        }
    }



val SOCKETS_HOST: String
    get() {
        return when (VARIANT) {
            PROD_VARIANT -> SOCKETS_HOST_PROD
            DEV_VARIANT -> SOCKETS_HOST_DEV
            else -> throw IllegalArgumentException("Unknown variant: $VARIANT")
        }
    }

const val SOCKETS_PORT = 8080
const val SOCKETS_PATH = "/api/v1"

val HABITS_ENDPOINT = "$BASE_URL/habits"
val TASKS_ENDPOINT = "$BASE_URL/tasks"
val TIMER_ENDPOINT = "$BASE_URL/timers"
val LOGIN_ENDPOINT = "$BASE_URL/auth/login"
val SIGNUP_ENDPOINT = "$BASE_URL/auth/register"

fun HttpMessageBuilder.appHeaders(token: String?): HeadersBuilder {
    return this.headers {
        append(HttpHeaders.Authorization, "Bearer $token")
        append(HttpHeaders.Connection, "Keep-Alive")
        append(HttpHeaders.AcceptEncoding, "gzip")
        append(HttpHeaders.UserAgent, "okhttp/5.0.0-alpha.2")
    }
}
