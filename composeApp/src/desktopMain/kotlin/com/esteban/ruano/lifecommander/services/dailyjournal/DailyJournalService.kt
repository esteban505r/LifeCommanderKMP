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
        val questions = response.body<List<QuestionDTO>>()
        println("DEBUG: Received questions: $questions")
        return questions
    }

    suspend fun addQuestion(text: String, type: QuestionType = QuestionType.TEXT): Unit {
        httpClient.post("$baseUrl/questions") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(mapOf("question" to text, "type" to type.name))
        }
    }

    suspend fun updateQuestion(id: String, text: String, type: QuestionType = QuestionType.TEXT): QuestionDTO {
        val response = httpClient.patch("$baseUrl/questions/$id") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(mapOf("question" to text, "type" to type.name))
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
    ) {
        val response = httpClient.post("$baseUrl/daily-journals") {
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(CreateDailyJournalDTO(date, summary, questionAnswers))
        }
        if(response.status != HttpStatusCode.Created) {
            throw Exception("Failed to create daily journal: ${response.status}")
        }
    }

    suspend fun getByDateRange(
        startDate : String,
        endDate : String,
        limit: Int = 10,
        offset: Long = 0): List<DailyJournalResponse> {
        val response = httpClient.get("$baseUrl/daily-journals/byDateRange"){
            header("Authorization", "Bearer ${tokenStorageImpl.getToken()}")
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("limit", limit)
            parameter("offset", offset)
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to fetch daily journals: ${response.status}")
        }
        return response.body()
    }
}