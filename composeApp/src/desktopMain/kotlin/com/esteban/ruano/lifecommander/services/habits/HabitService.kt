package services.habits

import com.esteban.ruano.lifecommander.services.habits.HABITS_ENDPOINT
import com.esteban.ruano.lifecommander.services.habits.appHeaders
import encodeUrlWithSpaces
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.HabitResponses.HabitUtils.time
import services.habits.models.HabitRequest
import services.habits.models.HabitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitService(
    private val client: HttpClient
) : HabitRepository {

    override suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<HabitResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(HABITS_ENDPOINT) {
                    url {
                        parameters.append("filter", "")
                        parameters.append("page", page.toString())
                        parameters.append("limit", limit.toString())
                        parameters.append("date", date)
                    }
                    appHeaders(token)
                }.body<List<HabitResponse>>().sortedBy { it.time() }
                response
            } catch (e: Exception) {
                throw HabitServiceException("Failed to fetch habits: ${e.message}", e)
            }
            }
    }

    override suspend fun completeHabit(token: String, id: String, dateTime: String) {
        withContext(Dispatchers.IO) {
        try {
            val parameters = Parameters.build {
                    append("datetime", dateTime)
            }
            val encodedUrl = encodeUrlWithSpaces("${HABITS_ENDPOINT}/$id/complete", parameters)
            val response = client.patch(encodedUrl) {
                appHeaders(token)
            }
                if (response.status != HttpStatusCode.OK) {
                    throw HabitServiceException("Failed to complete habit: ${response.status}")
            }
        } catch (e: Exception) {
                throw HabitServiceException("Failed to complete habit: ${e.message}", e)
        }
    }
    }

    override suspend fun unCompleteHabit(token: String, id: String, dateTime: String) {
        withContext(Dispatchers.IO) {
            try {
        val parameters = Parameters.build {
                    append("datetime", dateTime)
        }
        val encodedUrl = encodeUrlWithSpaces("${HABITS_ENDPOINT}/$id/uncomplete", parameters)
        val response = client.patch(encodedUrl) {
            appHeaders(token)
        }
                if (response.status != HttpStatusCode.OK) {
                    throw HabitServiceException("Failed to uncomplete habit: ${response.status}")
            }
        } catch (e: Exception) {
                throw HabitServiceException("Failed to uncomplete habit: ${e.message}", e)
        }
    }
    }

    override suspend fun addHabit(token: String, name: String, note: String?, frequency: String, dateTime: String) {
        withContext(Dispatchers.IO) {
        try {
            val response = client.post(HABITS_ENDPOINT) {
                appHeaders(token)
                contentType(ContentType.Application.Json)
                    setBody(HabitRequest(name = name, note = note, frequency = frequency, dateTime = dateTime, reminders = emptyList()))
            }
                if (response.status != HttpStatusCode.Created) {
                    throw HabitServiceException("Failed to add habit: ${response.status}")
                }
            } catch (e: Exception) {
                throw HabitServiceException("Failed to add habit: ${e.message}", e)
        }
    }
    }

    override suspend fun updateHabit(token: String, id: String, habit: HabitResponse) {
        withContext(Dispatchers.IO) {
        try {
            val response = client.patch("${HABITS_ENDPOINT}/$id") {
                appHeaders(token)
                contentType(ContentType.Application.Json)
                setBody(habit)
            }
                if (response.status != HttpStatusCode.OK) {
                    throw HabitServiceException("Failed to update habit: ${response.status}")
                }
            } catch (e: Exception) {
                throw HabitServiceException("Failed to update habit: ${e.message}", e)
        }
        }
    }

    override suspend fun deleteHabit(token: String, id: String) {
        withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${HABITS_ENDPOINT}/$id") {
                appHeaders(token)
            }
                if (response.status != HttpStatusCode.OK) {
                    throw HabitServiceException("Failed to delete habit: ${response.status}")
                }
            } catch (e: Exception) {
                throw HabitServiceException("Failed to delete habit: ${e.message}", e)
        }
        }
    }
}

class HabitServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)