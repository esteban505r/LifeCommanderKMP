package services.dailyjournal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import services.auth.TokenStorageImpl
import services.dailyjournal.models.*
import ui.services.dailyjournal.models.CreatePomodoroRequest

class DailyJournalService(
    private val baseUrl: String,
    private val tokenStorageImpl: TokenStorageImpl,
    private val httpClient: HttpClient
) {
    suspend fun getQuestions(): List<QuestionDTO> {
        val response = httpClient.get("$baseUrl/questions") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
        }
        return response.body()
    }

    suspend fun addQuestion(text: String): Unit {
        httpClient.post("$baseUrl/questions") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(mapOf("question" to text))
        }
    }

    suspend fun updateQuestion(id: String, text: String): QuestionDTO {
        val response = httpClient.put("$baseUrl/questions/$id") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(mapOf("text" to text))
        }
        return response.body()
    }

    suspend fun deleteQuestion(id: String): HttpResponse {
        return httpClient.delete("$baseUrl/questions/$id") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
        }
    }

    suspend fun createDailyJournal(
        date: String,
        summary: String,
        questionAnswers: List<QuestionAnswerDTO>
    ): DailyJournalResponse {
        val response = httpClient.post("$baseUrl/daily-journals") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(CreateDailyJournalDTO(date, summary, questionAnswers))
        }
        return response.body()
    }

    suspend fun getPomodoros(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 30,
    ): List<PomodoroResponse> {
        val response = httpClient.get("$baseUrl/pomodoros") {
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