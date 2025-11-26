package services.study

import com.esteban.ruano.lifecommander.utils.appHeaders
import com.lifecommander.models.StudyItem
import com.lifecommander.models.StudySession
import com.lifecommander.models.StudyStats
import com.lifecommander.models.StudyTopic
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl

class StudyService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    // StudyTopic operations
    suspend fun getTopics(isActive: Boolean? = null): List<StudyTopic> {
        return httpClient.get("$baseUrl/study/topics") {
            appHeaders(tokenStorageImpl.getToken())
            isActive?.let { parameter("isActive", it) }
        }.body()
    }

    suspend fun getTopicById(id: String): StudyTopic {
        return httpClient.get("$baseUrl/study/topics/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun createTopic(topic: StudyTopic): String {
        val response = httpClient.post("$baseUrl/study/topics") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "name" to (topic.name),
                "description" to (topic.description ?: ""),
                "discipline" to (topic.discipline ?: ""),
                "color" to (topic.color ?: ""),
                "icon" to (topic.icon ?: ""),
                "isActive" to topic.isActive
            ))
        }
        val result = response.body<Map<String, String>>()
        return result["id"] ?: throw Exception("Failed to create topic")
    }

    suspend fun updateTopic(id: String, topic: StudyTopic) {
        httpClient.patch("$baseUrl/study/topics/$id") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(topic)
        }
    }

    suspend fun deleteTopic(id: String) {
        httpClient.delete("$baseUrl/study/topics/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // StudyItem operations
    suspend fun getItems(topicId: String? = null, stage: String? = null, search: String? = null): List<StudyItem> {
        return httpClient.get("$baseUrl/study/items") {
            appHeaders(tokenStorageImpl.getToken())
            topicId?.let { parameter("topicId", it) }
            stage?.let { parameter("stage", it) }
            search?.let { parameter("search", it) }
        }.body()
    }

    suspend fun getItemById(id: String): StudyItem {
        return httpClient.get("$baseUrl/study/items/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun createItem(item: StudyItem): String {
        // Extract topicId from topic object if provided, otherwise use null
        val topicId = item.topic?.id ?: null
        
        val response = httpClient.post("$baseUrl/study/items") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "topicId" to (topicId ?: ""),
                "title" to item.title,
                "obsidianPath" to (item.obsidianPath ?: ""),
                "stage" to item.stage,
                "modeHint" to (item.modeHint ?: ""),
                "discipline" to (item.discipline ?: ""),
                "progress" to item.progress,
                "estimatedEffortMinutes" to (item.estimatedEffortMinutes ?: 0)
            ))
        }
        val result = response.body<Map<String, String>>()
        return result["id"] ?: throw Exception("Failed to create item")
    }

    suspend fun updateItem(id: String, item: StudyItem) {
        // Extract topicId from topic object if provided
        val topicId = item.topic?.id
        
        httpClient.patch("$baseUrl/study/items/$id") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "topicId" to (topicId ?: ""),
                "title" to item.title,
                "obsidianPath" to (item.obsidianPath ?: ""),
                "stage" to item.stage,
                "modeHint" to (item.modeHint ?: ""),
                "discipline" to (item.discipline ?: ""),
                "progress" to item.progress,
                "estimatedEffortMinutes" to (item.estimatedEffortMinutes ?: 0)
            ))
        }
    }

    suspend fun deleteItem(id: String) {
        httpClient.delete("$baseUrl/study/items/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // StudySession operations
    suspend fun getSessions(
        topicId: String? = null,
        mode: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<StudySession> {
        return httpClient.get("$baseUrl/study/sessions") {
            appHeaders(tokenStorageImpl.getToken())
            topicId?.let { parameter("topicId", it) }
            mode?.let { parameter("mode", it) }
            startDate?.let { parameter("startDate", it) }
            endDate?.let { parameter("endDate", it) }
        }.body()
    }

    suspend fun getSessionById(id: String): StudySession {
        return httpClient.get("$baseUrl/study/sessions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun createSession(session: StudySession): String {
        // Extract IDs from objects if provided
        val topicId = session.topic?.id ?: null
        val studyItemId = session.studyItem?.id ?: null
        
        val response = httpClient.post("$baseUrl/study/sessions") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "topicId" to (topicId ?: ""),
                "studyItemId" to (studyItemId ?: ""),
                "mode" to session.mode,
                "plannedStart" to (session.plannedStart ?: ""),
                "plannedEnd" to (session.plannedEnd ?: ""),
                "actualStart" to (session.actualStart ?: ""),
                "notes" to (session.notes ?: "")
            ))
        }
        val result = response.body<Map<String, String>>()
        return result["id"] ?: throw Exception("Failed to create session")
    }

    suspend fun updateSession(id: String, session: StudySession) {
        // Extract IDs from objects if provided
        val topicId = session.topic?.id
        val studyItemId = session.studyItem?.id
        
        httpClient.patch("$baseUrl/study/sessions/$id") {
            appHeaders(tokenStorageImpl.getToken())
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "topicId" to (topicId ?: ""),
                "studyItemId" to (studyItemId ?: ""),
                "mode" to session.mode,
                "plannedStart" to (session.plannedStart ?: ""),
                "plannedEnd" to (session.plannedEnd ?: ""),
                "actualStart" to (session.actualStart ?: ""),
                "actualEnd" to (session.actualEnd ?: ""),
                "durationMinutes" to (session.durationMinutes ?: 0),
                "notes" to (session.notes ?: "")
            ))
        }
    }

    suspend fun completeSession(id: String, actualEnd: String, notes: String? = null) {
        httpClient.post("$baseUrl/study/sessions/$id/complete") {
            appHeaders(tokenStorageImpl.getToken())
            parameter("actualEnd", actualEnd)
            notes?.let { parameter("notes", it) }
        }
    }

    suspend fun deleteSession(id: String) {
        httpClient.delete("$baseUrl/study/sessions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Stats
    suspend fun getStats(startDate: String? = null, endDate: String? = null): StudyStats {
        return httpClient.get("$baseUrl/study/stats") {
            appHeaders(tokenStorageImpl.getToken())
            startDate?.let { parameter("startDate", it) }
            endDate?.let { parameter("endDate", it) }
        }.body()
    }

    // Disciplines
    suspend fun getDisciplines(): List<String> {
        return httpClient.get("$baseUrl/study/disciplines") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }
}

