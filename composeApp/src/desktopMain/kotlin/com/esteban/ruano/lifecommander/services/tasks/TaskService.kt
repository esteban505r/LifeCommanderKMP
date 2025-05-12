package services.tasks

import encodeUrlWithSpaces
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.esteban.ruano.lifecommander.services.habits.TASKS_ENDPOINT
import com.esteban.ruano.lifecommander.services.habits.appHeaders
import com.esteban.ruano.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskService(
    private val client: HttpClient
) : TaskRepository {

    override suspend fun getByDate(token: String, page: Int, limit: Int, date: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(TASKS_ENDPOINT) {
                    url {
                        parameters.append("filter", "")
                        parameters.append("page", page.toString())
                        parameters.append("limit", limit.toString())
                        parameters.append("date", date)
                    }
                    appHeaders(token)
                }.body<List<Task>>()
                response
            } catch (e: Exception) {
                throw TaskServiceException("Failed to fetch tasks: ${e.message}", e)
            }
        }
    }

    override suspend fun getByDateRange(token: String, page: Int, limit: Int, startDate: String, endDate: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val parameters = Parameters.build {
                    append("filter", "")
                    append("page", page.toString())
                    append("limit", limit.toString())
                    append("startDate", startDate)
                    append("endDate", endDate)
                }
                val encodedUrl = encodeUrlWithSpaces("$TASKS_ENDPOINT/byDateRange", parameters)
                val response = client.get(encodedUrl) {
                    appHeaders(token)
                }.body<List<Task>>()
                response
            } catch (e: Exception) {
                throw TaskServiceException("Failed to fetch tasks by date range: ${e.message}", e)
            }
        }
    }

    override suspend fun completeTask(token: String, id: String, dateTime: String) {
        withContext(Dispatchers.IO) {
            try {
                val parameters = Parameters.build {
                    append("datetime", dateTime)
                }
                val encodedUrl = encodeUrlWithSpaces("$TASKS_ENDPOINT/$id/complete", parameters)
                val response = client.patch(encodedUrl) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TaskServiceException("Failed to complete task: ${response.status}")
                }
            } catch (e: Exception) {
                throw TaskServiceException("Failed to complete task: ${e.message}", e)
            }
        }
    }

    override suspend fun unCompleteTask(token: String, id: String, dateTime: String) {
        withContext(Dispatchers.IO) {
            try {
                val parameters = Parameters.build {
                    append("datetime", dateTime)
                }
                val encodedUrl = encodeUrlWithSpaces("$TASKS_ENDPOINT/$id/uncomplete", parameters)
                val response = client.patch(encodedUrl) {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TaskServiceException("Failed to uncomplete task: ${response.status}")
                }
            } catch (e: Exception) {
                throw TaskServiceException("Failed to uncomplete task: ${e.message}", e)
            }
        }
    }

    override suspend fun getAll(token: String, page: Int, limit: Int): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(TASKS_ENDPOINT) {
                    url {
                        parameters.append("filter", "")
                        parameters.append("page", page.toString())
                        parameters.append("limit", limit.toString())
                    }
                    appHeaders(token)
                }.body<List<Task>>()
                response
            } catch (e: Exception) {
                throw TaskServiceException("Failed to fetch all tasks: ${e.message}", e)
            }
        }
    }

    override suspend fun addTask(token: String, name: String, dueDate: String?, scheduledDate: String?, note: String?, priority: Int) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.post(TASKS_ENDPOINT) {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(Task(
                        id = "",
                        name = name,
                        done = false,
                        note = note ?: "",
                        dueDateTime = dueDate,
                        scheduledDateTime = scheduledDate,
                        reminders = null,
                        priority = priority
                    ))
                }
                if (response.status != HttpStatusCode.Created) {
                    throw TaskServiceException("Failed to add task: ${response.status}")
                }
            } catch (e: Exception) {
                throw TaskServiceException("Failed to add task: ${e.message}", e)
            }
        }
    }

    override suspend fun getNoDueDateTasks(token: String, page: Int, limit: Int): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get("$TASKS_ENDPOINT/noDueDate") {
                    url {
                        parameters.append("filter", "")
                        parameters.append("page", page.toString())
                        parameters.append("limit", limit.toString())
                    }
                    appHeaders(token)
                }.body<List<Task>>()
                response
            } catch (e: Exception) {
                throw TaskServiceException("Failed to fetch tasks with no due date: ${e.message}", e)
            }
        }
    }

    override suspend fun updateTask(token: String, id: String, task: Task) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$TASKS_ENDPOINT/$id") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(task)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TaskServiceException("Failed to update task: ${response.status}")
                }
            } catch (e: Exception) {
                throw TaskServiceException("Failed to update task: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteTask(token: String, id: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.delete("$TASKS_ENDPOINT/$id") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TaskServiceException("Failed to delete task: ${response.status}")
                }
            } catch (e: Exception) {
                throw TaskServiceException("Failed to delete task: ${e.message}", e)
            }
        }
    }
}

class TaskServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)