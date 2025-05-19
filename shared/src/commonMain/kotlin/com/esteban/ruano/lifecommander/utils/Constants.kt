package com.esteban.ruano.lifecommander.utils

import io.ktor.client.request.*
import io.ktor.http.*

//const val BASE_URL = "http://64.23.239.161:8080/api/v1"
const val BASE_URL = "http://ec2-3-91-21-254.compute-1.amazonaws.com:8080/api/v1"
//const val BASE_URL = "http://localhost:8080/api/v1"
const val SOCKETS_HOST = "localhost"
const val SOCKETS_PORT = 8080
const val SOCKETS_PATH = "/api/v1"

const val HABITS_ENDPOINT = "$BASE_URL/habits"
const val TASKS_ENDPOINT = "$BASE_URL/tasks"
const val TIMER_ENDPOINT = "$BASE_URL/timers"
const val LOGIN_ENDPOINT = "$BASE_URL/auth/login"
const val SIGNUP_ENDPOINT = "$BASE_URL/auth/register"

fun HttpMessageBuilder.appHeaders(token: String?): HeadersBuilder {
    return this.headers {
        append(HttpHeaders.Authorization, "Bearer $token")
        append(HttpHeaders.Connection, "Keep-Alive")
        append(HttpHeaders.AcceptEncoding, "gzip")
        append(HttpHeaders.UserAgent, "okhttp/5.0.0-alpha.2")
    }
}
