package services.dailyjournal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import services.auth.TokenStorageImpl
import services.dailyjournal.models.*
import ui.services.dailyjournal.models.CreatePomodoroRequest

class PomodoroService(
    private val baseUrl: String,
    private val tokenStorageImpl: TokenStorageImpl,
    private val httpClient: HttpClient
) {
    suspend fun getPomodoros(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 30,
    ): List<PomodoroResponse> {
        val response = httpClient.get("$baseUrl/pomodoros/byDateRange") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("limit", limit)
        }
        return response.body()
    }

    suspend fun removePomodoro(id: String): HttpResponse {
        return httpClient.delete("$baseUrl/pomodoros/$id") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
        }
    }

    suspend fun createPomodoro(pomodoro: CreatePomodoroRequest): Any {
        return httpClient.post("$baseUrl/pomodoros") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(pomodoro)
        }.body()
    }
}