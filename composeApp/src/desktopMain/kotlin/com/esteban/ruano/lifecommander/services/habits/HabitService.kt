package services.habits

import com.esteban.ruano.lifecommander.utils.HABITS_ENDPOINT
import com.esteban.ruano.lifecommander.utils.appHeaders
import com.lifecommander.models.Habit
import encodeUrlWithSpaces
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitService(
    private val client: HttpClient
) : HabitRepository {

    override suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<Habit> {
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
                }.body<List<Habit>>().sortedBy { it.dateTime }
                response
            } catch (e: Exception) {
                throw HabitServiceException("Failed to fetch habits: ${e.message}", e)
            }
        }
    }

    override suspend fun getByDateRange(token: String, page: Int, limit: Int, startDate: String, endDate: String, excludeDaily: Boolean): List<Habit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(HABITS_ENDPOINT) {
                    url {
                        parameters.append("filter", "")
                        parameters.append("page", page.toString())
                        parameters.append("limit", limit.toString())
                        parameters.append("startDate", startDate)
                        parameters.append("endDate", endDate)
                        parameters.append("excludeDaily", excludeDaily.toString())
                    }
                    appHeaders(token)
                }.body<List<Habit>>().sortedBy { it.dateTime }
                response
            } catch (e: Exception) {
                throw HabitServiceException("Failed to fetch habits by date range: ${e.message}", e)
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
                    setBody(Habit(
                        id = "",
                        name = name,
                        note = note,
                        dateTime = dateTime,
                        done = false,
                        frequency = frequency
                    ))
                }
                if (response.status != HttpStatusCode.Created) {
                    throw HabitServiceException("Failed to add habit: ${response.status}")
                }
            } catch (e: Exception) {
                throw HabitServiceException("Failed to add habit: ${e.message}", e)
            }
        }
    }

    override suspend fun updateHabit(token: String, id: String, habit: Habit) {
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